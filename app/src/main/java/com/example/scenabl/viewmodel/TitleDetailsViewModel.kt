package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.model.PerformanceStatus
import com.example.scenabl.data.model.Recenzija
import com.example.scenabl.data.repository.PerformanceRepository
import com.example.scenabl.data.repository.ReviewRepository
import com.example.scenabl.data.repository.TitleRepository
import com.example.scenabl.data.repository.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A review paired with the display name resolved from its author's user document. */
data class ReviewItem(
    val recenzija: Recenzija,
    val reviewerName: String
)

data class TitleDetailsUiState(
    val naslov: Naslov? = null,
    val institutionName: String = "",
    val upcomingPerformances: List<Izvodjenje> = emptyList(),
    val reviews: List<ReviewItem> = emptyList(),
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class TitleDetailsViewModel(
    private val titleId: String,
    private val titleRepository: TitleRepository,
    private val performanceRepository: PerformanceRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TitleDetailsUiState())
    val uiState: StateFlow<TitleDetailsUiState> = _uiState.asStateFlow()

    private val reviewerNames = mutableMapOf<String, String>()

    init {
        viewModelScope.launch {
            titleRepository.observeTitle(titleId).collect { naslov ->
                _uiState.update { it.copy(naslov = naslov, isLoading = false) }
            }
        }

        viewModelScope.launch {
            titleRepository.observeTitle(titleId)
                .map { it?.institutionId }
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { institutionId -> userRepository.observeInstitution(institutionId) }
                .collect { institucija -> _uiState.update { it.copy(institutionName = institucija?.naziv ?: "") } }
        }

        viewModelScope.launch {
            performanceRepository.observePerformancesForTitle(titleId).collect { performances ->
                val now = Timestamp.now()
                val upcoming = performances
                    .filter { it.status == PerformanceStatus.SCHEDULED && it.datumVrijeme >= now }
                    .sortedBy { it.datumVrijeme }
                _uiState.update { it.copy(upcomingPerformances = upcoming) }
            }
        }

        viewModelScope.launch {
            reviewRepository.observeReviewsForTitle(titleId).collect { reviews ->
                val sorted = reviews.sortedByDescending { it.datum }
                val average = if (reviews.isEmpty()) 0.0 else reviews.map { it.ocjena }.average()
                _uiState.update {
                    it.copy(
                        reviews = sorted.map { r -> ReviewItem(r, reviewerNames[r.userId] ?: "Korisnik") },
                        averageRating = average,
                        reviewCount = reviews.size
                    )
                }
                loadMissingReviewerNames(sorted.map { it.userId }.distinct())
            }
        }
    }

    private fun loadMissingReviewerNames(userIds: List<String>) = viewModelScope.launch {
        val missing = userIds.filter { it !in reviewerNames }
        if (missing.isEmpty()) return@launch
        missing.forEach { uid ->
            userRepository.getUser(uid).getOrNull()?.let { korisnik ->
                reviewerNames[uid] = "${korisnik.ime} ${korisnik.prezime}".trim()
            }
        }
        _uiState.update { state ->
            state.copy(
                reviews = state.reviews.map {
                    it.copy(reviewerName = reviewerNames[it.recenzija.userId] ?: it.reviewerName)
                }
            )
        }
    }
}
