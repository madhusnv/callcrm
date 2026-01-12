package com.educonsult.crm.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educonsult.crm.data.remote.dto.template.NoteTemplateDto
import com.educonsult.crm.domain.repository.CallRepository
import com.educonsult.crm.domain.repository.LeadRepository
import com.educonsult.crm.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class NotePopupViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val templateRepository: TemplateRepository,
    private val leadRepository: LeadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotePopupUiState())
    val uiState: StateFlow<NotePopupUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTemplates = true) }
            
            templateRepository.getNoteTemplates()
                .onSuccess { templates ->
                    val grouped = templates.groupBy { it.category }
                    _uiState.update { 
                        it.copy(
                            templates = templates,
                            templatesByCategory = grouped,
                            isLoadingTemplates = false
                        ) 
                    }
                }
                .onFailure {
                    _uiState.update { 
                        it.copy(
                            isLoadingTemplates = false,
                            useDefaultNotes = true
                        ) 
                    }
                }
        }
    }

    fun updateNoteText(text: String) {
        _uiState.update { it.copy(noteText = text, selectedTemplate = null) }
    }

    fun selectTemplate(template: NoteTemplateDto) {
        _uiState.update { 
            it.copy(
                noteText = template.content,
                selectedTemplate = template
            ) 
        }
    }

    fun selectStatus(status: LeadStatusOption) {
        _uiState.update { 
            it.copy(selectedStatus = if (it.selectedStatus == status) null else status)
        }
    }

    fun toggleFollowUp() {
        _uiState.update { it.copy(showFollowUp = !it.showFollowUp) }
    }

    fun selectFollowUpDays(days: Int) {
        val followUpDate = LocalDateTime.now()
            .plusDays(days.toLong())
            .with(LocalTime.of(10, 0)) // Default to 10 AM
        _uiState.update { 
            it.copy(
                selectedFollowUpDays = days,
                followUpDate = followUpDate
            ) 
        }
    }

    fun updateFollowUpNote(note: String) {
        _uiState.update { it.copy(followUpNote = note) }
    }

    fun saveNote(callLogId: String, leadId: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val state = _uiState.value
                
                // Save call note
                val note = state.noteText
                if (note.isNotBlank()) {
                    callRepository.updateCallLogNotes(callLogId, note)
                }

                // Update lead status if selected
                if (leadId != null && state.selectedStatus != null) {
                    leadRepository.updateLeadStatus(leadId, state.selectedStatus.apiValue)
                }

                // Schedule follow-up if set
                if (leadId != null && state.followUpDate != null) {
                    leadRepository.scheduleFollowUp(
                        leadId = leadId,
                        followUpDate = state.followUpDate,
                        reminderNote = state.followUpNote.ifBlank { "Follow-up scheduled after call" }
                    )
                }
            } catch (e: Exception) {
                // Log error but continue
            } finally {
                _uiState.update { it.copy(isSaving = false) }
                onComplete()
            }
        }
    }
}

data class NotePopupUiState(
    // Note
    val noteText: String = "",
    val templates: List<NoteTemplateDto> = emptyList(),
    val templatesByCategory: Map<String, List<NoteTemplateDto>> = emptyMap(),
    val selectedTemplate: NoteTemplateDto? = null,
    val isLoadingTemplates: Boolean = false,
    val useDefaultNotes: Boolean = false,
    
    // Status
    val selectedStatus: LeadStatusOption? = null,
    
    // Follow-up
    val showFollowUp: Boolean = false,
    val selectedFollowUpDays: Int? = null,
    val followUpDate: LocalDateTime? = null,
    val followUpNote: String = "",
    
    // Saving
    val isSaving: Boolean = false
) {
    val defaultQuickNotes = listOf(
        "Discussed requirements",
        "Follow up needed",
        "Interested - send brochure",
        "Not interested",
        "Call back later",
        "No answer"
    )
    
    val followUpOptions = listOf(
        1 to "Tomorrow",
        2 to "In 2 days",
        3 to "In 3 days",
        7 to "In 1 week",
        14 to "In 2 weeks"
    )
}

enum class LeadStatusOption(val displayName: String, val apiValue: String) {
    NEW("New", "new"),
    CONTACTED("Contacted", "contacted"),
    INTERESTED("Interested", "interested"),
    NOT_INTERESTED("Not Interested", "not_interested"),
    FOLLOW_UP("Follow Up", "follow_up"),
    CONVERTED("Converted", "converted")
}
