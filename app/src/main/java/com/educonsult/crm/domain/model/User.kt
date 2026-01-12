package com.educonsult.crm.domain.model

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val role: UserRole,
    val organizationId: String,
    val branchId: String?
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

enum class UserRole {
    SUPER_ADMIN,
    ADMIN,
    MANAGER,
    COUNSELOR,
    TELECALLER
}
