package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.repository.LeadRepository
import javax.inject.Inject

class GetLeadByIdUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    suspend operator fun invoke(id: String): Result<Lead> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("Lead ID cannot be empty"))
        }
        
        return leadRepository.getLeadById(id)?.let {
            Result.success(it)
        } ?: Result.failure(NoSuchElementException("Lead not found"))
    }
}
