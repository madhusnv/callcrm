package com.educonsult.crm.domain.usecase.education

import com.educonsult.crm.domain.model.Course
import com.educonsult.crm.domain.repository.EducationRepository
import javax.inject.Inject

class GetCoursesUseCase @Inject constructor(
    private val educationRepository: EducationRepository
) {
    suspend operator fun invoke(
        countryId: String? = null,
        institutionId: String? = null,
        level: String? = null,
        includeInactive: Boolean = false,
        search: String? = null
    ): Result<List<Course>> {
        return educationRepository.getCourses(
            countryId = countryId,
            institutionId = institutionId,
            level = level,
            includeInactive = includeInactive,
            search = search
        )
    }
}
