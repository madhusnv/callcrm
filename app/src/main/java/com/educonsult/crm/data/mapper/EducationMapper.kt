package com.educonsult.crm.data.mapper

import com.educonsult.crm.data.remote.dto.education.response.CountryDto
import com.educonsult.crm.data.remote.dto.education.response.CourseDto
import com.educonsult.crm.data.remote.dto.education.response.InstitutionDto
import com.educonsult.crm.domain.model.Country
import com.educonsult.crm.domain.model.Course
import com.educonsult.crm.domain.model.Institution

fun CountryDto.toDomain(): Country {
    return Country(
        id = id,
        name = name,
        code = code,
        currencyCode = currencyCode,
        phoneCode = phoneCode,
        displayOrder = displayOrder,
        isActive = isActive
    )
}

fun InstitutionDto.toDomain(): Institution {
    return Institution(
        id = id,
        countryId = countryId,
        name = name,
        city = city,
        institutionType = institutionType,
        website = website,
        logoUrl = logoUrl,
        displayOrder = displayOrder,
        isActive = isActive
    )
}

fun CourseDto.toDomain(): Course {
    return Course(
        id = id,
        countryId = countryId,
        institutionId = institutionId,
        name = name,
        level = level,
        durationMonths = durationMonths,
        intakeMonths = intakeMonths,
        tuitionFee = tuitionFee,
        currencyCode = currencyCode,
        description = description,
        displayOrder = displayOrder,
        isActive = isActive
    )
}
