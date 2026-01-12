package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.repository.LeadRepository
import javax.inject.Inject

class DeleteLeadUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("Lead ID cannot be empty"))
        }

        return leadRepository.deleteLead(id)
    }
}
