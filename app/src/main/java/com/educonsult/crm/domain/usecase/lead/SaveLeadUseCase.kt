package com.educonsult.crm.domain.usecase.lead

import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.repository.LeadRepository
import javax.inject.Inject

class SaveLeadUseCase @Inject constructor(
    private val leadRepository: LeadRepository
) {
    suspend operator fun invoke(lead: Lead): Result<Lead> {
        if (lead.firstName.isBlank()) {
            return Result.failure(IllegalArgumentException("First name cannot be empty"))
        }
        if (lead.phone.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number cannot be empty"))
        }
        if (!isValidPhone(lead.phone)) {
            return Result.failure(IllegalArgumentException("Invalid phone number format"))
        }
        lead.email?.let { email ->
            if (email.isNotBlank() && !isValidEmail(email)) {
                return Result.failure(IllegalArgumentException("Invalid email format"))
            }
        }

        return leadRepository.saveLead(lead)
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
