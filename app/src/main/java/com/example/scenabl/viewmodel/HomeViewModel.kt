package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Institucija
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.model.Recenzija
import com.example.scenabl.data.repository.PerformanceRepository
import com.example.scenabl.data.repository.ReviewRepository
import com.example.scenabl.data.repository.TitleRepository
import com.example.scenabl.data.repository.UserRepository
import com.example.scenabl.ui.util.toLocalDate
import com.google.firebase.Timestamp
import java.time.LocalDate
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One row on HomeScreen: a performance joined with its title, institution name and rating. */
data class RepertoireItem(
    val performance: Izvodjenje,
    val title: Naslov,
    val institutionName: String,
    val averageRating: Double,
    val reviewCount: Int
)

data class HomeUiState(
    val searchQuery: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val selectedType: String? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val items: List<RepertoireItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedGenres.isNotEmpty() || selectedType != null ||
            dateFrom != null || dateTo != null
}

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val performanceRepository: PerformanceRepository,
    private val titleRepository: TitleRepository,
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private val genresFlow = MutableStateFlow<Set<String>>(emptySet())
    private val typeFlow = MutableStateFlow<String?>(null)
    private val dateRangeFlow = MutableStateFlow<Pair<LocalDate?, LocalDate?>>(null to null)

    init {
        viewModelScope.launch {
            val rawItems = combine(
                performanceRepository.observeUpcomingPerformances(),
                titleRepository.observeTitles(),
                userRepository.observeInstitutions(),
                reviewRepository.observeAllReviews()
            ) { performances, titles, institutions, reviews ->
                buildItems(performances, titles, institutions, reviews)
            }

            combine(
                rawItems,
                queryFlow.debounce(300).onStart { emit("") },
                genresFlow,
                typeFlow,
                dateRangeFlow
            ) { items, query, genres, type, dateRange ->
                filterItems(items, query, genres, type, dateRange)
            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { filtered -> _uiState.update { it.copy(items = filtered, isLoading = false, errorMessage = null) } }
        }
    }

    fun onSearchQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
        queryFlow.value = value
    }

    fun onGenreToggle(genre: String) {
        val updated = if (genre in _uiState.value.selectedGenres) {
            _uiState.value.selectedGenres - genre
        } else {
            _uiState.value.selectedGenres + genre
        }
        _uiState.update { it.copy(selectedGenres = updated) }
        genresFlow.value = updated
    }

    fun onTypeSelected(type: String?) {
        _uiState.update { it.copy(selectedType = type) }
        typeFlow.value = type
    }

    fun onDateRangeSelected(from: LocalDate?, to: LocalDate?) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to) }
        dateRangeFlow.value = from to to
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(searchQuery = "", selectedGenres = emptySet(), selectedType = null, dateFrom = null, dateTo = null)
        }
        queryFlow.value = ""
        genresFlow.value = emptySet()
        typeFlow.value = null
        dateRangeFlow.value = null to null
    }

    private fun buildItems(
        performances: List<Izvodjenje>,
        titles: List<Naslov>,
        institutions: List<Institucija>,
        reviews: List<Recenzija>
    ): List<RepertoireItem> {
        val titleById = titles.associateBy { it.id }
        val institutionById = institutions.associateBy { it.id }
        val reviewsByTitle = reviews.groupBy { it.titleId }
        val now = Timestamp.now()

        return performances
            .filter { it.datumVrijeme >= now }
            .mapNotNull { performance ->
                val title = titleById[performance.titleId] ?: return@mapNotNull null
                val titleReviews = reviewsByTitle[title.id].orEmpty()
                RepertoireItem(
                    performance = performance,
                    title = title,
                    institutionName = institutionById[performance.institutionId]?.naziv ?: "",
                    averageRating = if (titleReviews.isEmpty()) 0.0 else titleReviews.map { it.ocjena }.average(),
                    reviewCount = titleReviews.size
                )
            }
            .sortedBy { it.performance.datumVrijeme }
    }

    private fun filterItems(
        items: List<RepertoireItem>,
        query: String,
        genres: Set<String>,
        type: String?,
        dateRange: Pair<LocalDate?, LocalDate?>
    ): List<RepertoireItem> {
        val (from, to) = dateRange
        return items.filter { item ->
            (query.isBlank() || item.title.naziv.contains(query, ignoreCase = true)) &&
                (genres.isEmpty() || item.title.zanr in genres) &&
                (type == null || item.title.tip == type) &&
                (from == null || !item.performance.datumVrijeme.toLocalDate().isBefore(from)) &&
                (to == null || !item.performance.datumVrijeme.toLocalDate().isAfter(to))
        }
    }
}
