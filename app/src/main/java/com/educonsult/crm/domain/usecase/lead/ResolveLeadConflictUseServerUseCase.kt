package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.repository.LeadRepository
import javax.inject.Inject

class ResolveLeadConflictUseServerUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    suspend operator fun invoke(leadId: String): Result<Unit> {
        return leadRepository.resolveConflictUseServer(leadId)
    }
}
