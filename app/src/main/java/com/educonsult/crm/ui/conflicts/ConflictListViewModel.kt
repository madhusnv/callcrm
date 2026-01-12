package com.educonsult.crm.ui.conflicts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.usecase.lead.GetConflictedLeadsUseCase
import com.educonsult.crm.domain.usecase.lead.ResolveLeadConflictKeepLocalUseCase
import com.educonsult.crm.domain.usecase.lead.ResolveLeadConflictUseServerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

data class ConflictListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val conflicts: List<Lead> = emptyList()
)

@HiltViewModel
class ConflictListViewModel @Inject constructor(
    private val getConflictedLeadsUseCase: GetConflictedLeadsUseCase,
    private val resolveKeepLocalUseCase: ResolveLeadConflictKeepLocalUseCase,
    private val resolveUseServerUseCase: ResolveLeadConflictUseServerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConflictListUiState())
    val uiState: StateFlow<ConflictListUiState> = _uiState.asStateFlow()

    init {
        observeConflicts()
    }

    fun resolveKeepLocal(leadId: String) {
        viewModelScope.launch {
            resolveKeepLocalUseCase(leadId)
        }
    }

    fun resolveUseServer(leadId: String) {
        viewModelScope.launch {
            resolveUseServerUseCase(leadId)
        }
    }

    private fun observeConflicts() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        getConflictedLeadsUseCase()
            .onEach { conflicts ->
                _uiState.update { it.copy(conflicts = conflicts, isLoading = false) }
            }
            .catch { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}
