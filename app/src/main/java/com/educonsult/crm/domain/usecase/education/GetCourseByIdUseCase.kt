package com.educonsult.crm.domain.usecase.education

import com.educonsult.crm.domain.model.Course
import com.educonsult.crm.domain.repository.EducationRepository
import javax.inject.Inject

class GetCourseByIdUseCase @Inject constructor(
    private val educationRepository: EducationRepository
) {
    suspend operator fun invoke(courseId: String): Result<Course> {
        return educationRepository.getCourseById(courseId)
    }
}
