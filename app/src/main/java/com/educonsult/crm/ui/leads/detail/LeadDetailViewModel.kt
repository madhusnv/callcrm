package com.educonsult.crm.ui.leads.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educonsult.crm.data.local.db.entity.CallLogEntity
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.model.LeadNote
import com.educonsult.crm.domain.model.NoteType
import com.educonsult.crm.domain.usecase.call.GetCallsByLeadUseCase
import com.educonsult.crm.domain.usecase.lead.DeleteLeadUseCase
import com.educonsult.crm.domain.usecase.lead.GetLeadByIdUseCase
import com.educonsult.crm.domain.usecase.lead.GetLeadNotesUseCase
import com.educonsult.crm.domain.usecase.lead.SaveLeadNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LeadDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLeadByIdUseCase: GetLeadByIdUseCase,
    private val getLeadNotesUseCase: GetLeadNotesUseCase,
    private val saveLeadNoteUseCase: SaveLeadNoteUseCase,
    private val deleteLeadUseCase: DeleteLeadUseCase,
    private val getCallsByLeadUseCase: GetCallsByLeadUseCase
) : ViewModel() {

    private val leadId: String = checkNotNull(savedStateHandle["leadId"])

    private val _uiState = MutableStateFlow(LeadDetailUiState())
    val uiState: StateFlow<LeadDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<LeadDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadLead()
        observeNotes()
        observeCalls()
    }

    private fun loadLead() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getLeadByIdUseCase(leadId)
                .onSuccess { lead ->
                    _uiState.update {
                        it.copy(
                            lead = lead,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load lead"
                        )
                    }
                }
        }
    }

    private fun observeNotes() {
        getLeadNotesUseCase(leadId)
            .onEach { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
            .catch { e ->
                _events.send(LeadDetailEvent.ShowError(e.message ?: "Failed to load notes"))
            }
            .launchIn(viewModelScope)
    }

    private fun observeCalls() {
        getCallsByLeadUseCase(leadId)
            .onEach { calls ->
                _uiState.update { it.copy(calls = calls) }
            }
            .catch { /* Silently fail for calls */ }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tab: LeadDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun addNote(content: String, noteType: NoteType = NoteType.GENERAL) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingNote = true) }

            val note = LeadNote(
                id = UUID.randomUUID().toString(),
                leadId = leadId,
                content = content.trim(),
                noteType = noteType,
                createdBy = "current_user",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            saveLeadNoteUseCase(leadId, note)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSavingNote = false,
                            newNoteContent = ""
                        )
                    }
                    _events.send(LeadDetailEvent.NoteAdded)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSavingNote = false) }
                    _events.send(LeadDetailEvent.ShowError(e.message ?: "Failed to save note"))
                }
        }
    }

    fun updateNewNoteContent(content: String) {
        _uiState.update { it.copy(newNoteContent = content) }
    }

    fun deleteLead() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            deleteLeadUseCase(leadId)
                .onSuccess {
                    _events.send(LeadDetailEvent.LeadDeleted)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeleting = false) }
                    _events.send(LeadDetailEvent.ShowError(e.message ?: "Failed to delete lead"))
                }
        }
    }

    fun refresh() {
        loadLead()
    }
}

data class LeadDetailUiState(
    val lead: Lead? = null,
    val notes: List<LeadNote> = emptyList(),
    val calls: List<CallLogEntity> = emptyList(),
    val selectedTab: LeadDetailTab = LeadDetailTab.OVERVIEW,
    val isLoading: Boolean = true,
    val isSavingNote: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val newNoteContent: String = ""
)

enum class LeadDetailTab {
    OVERVIEW,
    NOTES,
    CALLS,
    TIMELINE
}

sealed class LeadDetailEvent {
    data class ShowError(val message: String) : LeadDetailEvent()
    data object NoteAdded : LeadDetailEvent()
    data object LeadDeleted : LeadDetailEvent()
}
