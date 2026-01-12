package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.repository.LeadFilter
import com.educonsult.crm.domain.repository.LeadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLeadsUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    operator fun invoke(filter: LeadFilter? = null): Flow<List<Lead>> {
        return leadRepository.getLeads(filter)
    }
}
