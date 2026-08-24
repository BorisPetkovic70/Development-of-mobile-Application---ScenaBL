package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.KorisnickaLista
import com.example.scenabl.data.model.ListType
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.model.PerformanceStatus
import com.example.scenabl.data.model.Recenzija
import com.example.scenabl.data.repository.PerformanceRepository
import com.example.scenabl.data.repository.ReviewRepository
import com.example.scenabl.data.repository.TitleRepository
import com.example.scenabl.data.repository.UserListRepository
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
    val listEntry: KorisnickaLista? = null,
    val isTogglingList: Boolean = false,
    val myReview: Recenzija? = null,
    val isSubmittingReview: Boolean = false,
    val reviewErrorMessage: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    /** Reviewing is gated on the title being in the user's "Odgledano" list (REQ-REV-001). */
    val canReview: Boolean get() = listEntry?.tipListe == ListType.ODGLEDANO
}

@OptIn(ExperimentalCoroutinesApi::class)
class TitleDetailsViewModel(
    private val titleId: String,
    private val currentUserId: String?,
    private val titleRepository: TitleRepository,
    private val performanceRepository: PerformanceRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val userListRepository: UserListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TitleDetailsUiState())
    val uiState: StateFlow<TitleDetailsUiState> = _uiState.asStateFlow()

    private val reviewerNames = mutableMapOf<String, String>()

    init {
        val uid = currentUserId
        if (uid != null) {
            viewModelScope.launch {
                userListRepository.observeListEntry(uid, titleId).collect { entry ->
                    _uiState.update { it.copy(listEntry = entry) }
                }
            }
        }

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
                        reviewCount = reviews.size,
                        myReview = currentUserId?.let { uid -> reviews.find { r -> r.userId == uid } }
                    )
                }
                loadMissingReviewerNames(sorted.map { it.userId }.distinct())
            }
        }
    }

    /** Toggles this title's membership in the given list; setting one type overwrites the other (REQ-LIST-003). */
    fun onListToggle(tipListe: String) {
        val uid = currentUserId ?: return
        val current = _uiState.value.listEntry
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingList = true) }
            val result = if (current?.tipListe == tipListe) {
                userListRepository.removeListEntry(uid, titleId)
            } else {
                userListRepository.setListEntry(KorisnickaLista(userId = uid, titleId = titleId, tipListe = tipListe))
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isTogglingList = false) } },
                onFailure = { e -> _uiState.update { it.copy(isTogglingList = false, errorMessage = e.message) } }
            )
        }
    }

    /** Creates or edits (REQ-REV-002) the current user's review for this title. */
    fun submitReview(ocjena: Int, komentar: String) {
        val uid = currentUserId ?: return
        if (!_uiState.value.canReview || ocjena !in 1..5) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewErrorMessage = null) }
            val recenzija = Recenzija(userId = uid, titleId = titleId, ocjena = ocjena, komentar = komentar.take(500))
            reviewRepository.upsertReview(recenzija).fold(
                onSuccess = { _uiState.update { it.copy(isSubmittingReview = false) } },
                onFailure = { e -> _uiState.update { it.copy(isSubmittingReview = false, reviewErrorMessage = e.message) } }
            )
        }
    }

    fun deleteReview() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewErrorMessage = null) }
            reviewRepository.deleteReview(uid, titleId).fold(
                onSuccess = { _uiState.update { it.copy(isSubmittingReview = false) } },
                onFailure = { e -> _uiState.update { it.copy(isSubmittingReview = false, reviewErrorMessage = e.message) } }
            )
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
