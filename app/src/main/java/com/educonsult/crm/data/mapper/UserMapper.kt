package com.educonsult.crm.data.mapper

import com.educonsult.crm.data.local.db.entity.UserEntity
import com.educonsult.crm.data.remote.dto.response.LoginResponse
import com.educonsult.crm.domain.model.User
import com.educonsult.crm.domain.model.UserRole

fun LoginResponse.toUser(): User {
    return User(
        id = userId,
        email = "",
        firstName = "",
        lastName = "",
        phone = null,
        role = UserRole.TELECALLER,
        organizationId = organizationId,
        branchId = branchId
    )
}

fun LoginResponse.toUserEntity(): UserEntity {
    return UserEntity(
        id = userId,
        email = "",
        firstName = "",
        lastName = null,
        phone = null,
        role = UserRole.TELECALLER.name,
        organizationId = organizationId,
        branchId = branchId
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName ?: "",
        phone = phone,
        role = try {
            UserRole.valueOf(role)
        } catch (e: IllegalArgumentException) {
            UserRole.TELECALLER
        },
        organizationId = organizationId ?: "",
        branchId = branchId
    )
}
