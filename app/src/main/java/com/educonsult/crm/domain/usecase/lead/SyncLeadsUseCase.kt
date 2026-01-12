package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.repository.LeadRepository
import javax.inject.Inject

class SyncLeadsUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return leadRepository.syncLeads()
    }
}
