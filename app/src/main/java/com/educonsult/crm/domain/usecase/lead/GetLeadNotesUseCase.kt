package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.model.LeadNote
import com.educonsult.crm.domain.repository.LeadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLeadNotesUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    operator fun invoke(leadId: String): Flow<List<LeadNote>> {
        return leadRepository.getNotes(leadId)
    }
}
