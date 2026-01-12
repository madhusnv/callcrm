package com.educonsult.crm.ui.leads.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.model.LeadPriority
import com.educonsult.crm.domain.model.LeadStatus
import com.educonsult.crm.domain.repository.LeadFilter
import com.educonsult.crm.domain.usecase.lead.GetLeadsUseCase
import com.educonsult.crm.domain.usecase.lead.GetLeadStatusesUseCase
import com.educonsult.crm.domain.usecase.lead.SyncLeadsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeadListViewModel @Inject constructor(
    private val getLeadsUseCase: GetLeadsUseCase,
    private val getLeadStatusesUseCase: GetLeadStatusesUseCase,
    private val syncLeadsUseCase: SyncLeadsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadListUiState())
    val uiState: StateFlow<LeadListUiState> = _uiState.asStateFlow()

    private val _events = Channel<LeadListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val currentFilter = MutableStateFlow(LeadFilter())

    init {
        loadStatuses()
        observeLeads()
        syncLeads()
    }

    private fun loadStatuses() {
        getLeadStatusesUseCase()
            .onEach { statuses ->
                _uiState.update { it.copy(statuses = statuses) }
            }
            .catch { e ->
                _events.send(LeadListEvent.ShowError(e.message ?: "Failed to load statuses"))
            }
            .launchIn(viewModelScope)
    }

    private fun observeLeads() {
        currentFilter
            .flatMapLatest { filter ->
                getLeadsUseCase(filter)
                    .onStart {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
            }
            .onEach { leads ->
                _uiState.update {
                    it.copy(
                        leads = leads,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load leads"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun syncLeads() {
        viewModelScope.launch {
            syncLeadsUseCase()
                .onFailure { e ->
                    _events.send(LeadListEvent.ShowError(e.message ?: "Failed to sync leads"))
                }
        }
    }

    fun updateFilter(filter: LeadFilter) {
        currentFilter.value = filter
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        currentFilter.update { it.copy(searchQuery = query.ifBlank { null }) }
    }

    fun clearSearch() {
        search("")
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            syncLeadsUseCase()
                .onSuccess {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isRefreshing = false) }
                    _events.send(LeadListEvent.ShowError(e.message ?: "Failed to refresh leads"))
                }
        }
    }

    fun onLeadClick(leadId: String) {
        viewModelScope.launch {
            _events.send(LeadListEvent.NavigateToDetail(leadId))
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class LeadListUiState(
    val leads: List<Lead> = emptyList(),
    val statuses: List<LeadStatus> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: LeadFilter = LeadFilter()
) {
    val hasActiveFilters: Boolean
        get() = selectedFilter.statusId != null ||
                selectedFilter.priority != null ||
                selectedFilter.assignedTo != null ||
                selectedFilter.source != null
}

sealed class LeadListEvent {
    data class ShowError(val message: String) : LeadListEvent()
    data class NavigateToDetail(val leadId: String) : LeadListEvent()
}
