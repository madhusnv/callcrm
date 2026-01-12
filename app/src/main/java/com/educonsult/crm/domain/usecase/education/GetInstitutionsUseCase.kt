package com.educonsult.crm.domain.usecase.education

import com.educonsult.crm.domain.model.Institution
import com.educonsult.crm.domain.repository.EducationRepository
import javax.inject.Inject

class GetInstitutionsUseCase @Inject constructor(
    private val educationRepository: EducationRepository
) {
    suspend operator fun invoke(
        countryId: String? = null,
        includeInactive: Boolean = false,
        search: String? = null
    ): Result<List<Institution>> {
        return educationRepository.getInstitutions(
            countryId = countryId,
            includeInactive = includeInactive,
            search = search
        )
    }
}
