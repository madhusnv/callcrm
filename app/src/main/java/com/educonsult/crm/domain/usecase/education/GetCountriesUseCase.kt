package com.educonsult.crm.domain.usecase.education

import com.educonsult.crm.domain.model.Country
import com.educonsult.crm.domain.repository.EducationRepository
import javax.inject.Inject

class GetCountriesUseCase @Inject constructor(
    private val educationRepository: EducationRepository
) {
    suspend operator fun invoke(
        includeInactive: Boolean = false,
        search: String? = null
    ): Result<List<Country>> {
        return educationRepository.getCountries(
            includeInactive = includeInactive,
            search = search
        )
    }
}
