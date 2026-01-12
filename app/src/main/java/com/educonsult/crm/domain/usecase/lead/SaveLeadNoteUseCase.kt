package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.model.LeadNote
import com.educonsult.crm.domain.repository.LeadRepository
import javax.inject.Inject

class SaveLeadNoteUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    suspend operator fun invoke(leadId: String, note: LeadNote): Result<LeadNote> {
        if (leadId.isBlank()) {
            return Result.failure(IllegalArgumentException("Lead ID cannot be empty"))
        }
        if (note.content.isBlank()) {
            return Result.failure(IllegalArgumentException("Note content cannot be empty"))
        }

        return leadRepository.saveNote(leadId, note)
    }
}
