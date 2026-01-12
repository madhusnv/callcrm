package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.model.LeadStatus
import com.educonsult.crm.domain.repository.LeadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLeadStatusesUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    operator fun invoke(): Flow<List<LeadStatus>> {
        return leadRepository.getLeadStatuses()
    }
}
