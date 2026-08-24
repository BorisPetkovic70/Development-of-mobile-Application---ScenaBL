package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.KorisnickaLista
import com.example.scenabl.data.model.ListType
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.repository.TitleRepository
import com.example.scenabl.data.repository.UserListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListEntryItem(
    val entry: KorisnickaLista,
    val naslov: Naslov
)

data class MyListsUiState(
    val selectedTab: String = ListType.ZELIM_GLEDATI,
    val zelimGledati: List<ListEntryItem> = emptyList(),
    val odgledano: List<ListEntryItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MyListsViewModel(
    private val userId: String,
    private val userListRepository: UserListRepository,
    private val titleRepository: TitleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyListsUiState())
    val uiState: StateFlow<MyListsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userListRepository.observeUserLists(userId),
                titleRepository.observeTitles()
            ) { entries, titles ->
                val titleById = titles.associateBy { it.id }
                entries.mapNotNull { entry -> titleById[entry.titleId]?.let { ListEntryItem(entry, it) } }
                    .partition { it.entry.tipListe == ListType.ZELIM_GLEDATI }
            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { (zelimGledati, odgledano) ->
                    _uiState.update {
                        it.copy(zelimGledati = zelimGledati, odgledano = odgledano, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    fun onTabSelected(tab: String) = _uiState.update { it.copy(selectedTab = tab) }

    fun removeFromList(titleId: String) = viewModelScope.launch {
        userListRepository.removeListEntry(userId, titleId).fold(
            onSuccess = {},
            onFailure = { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        )
    }
}
