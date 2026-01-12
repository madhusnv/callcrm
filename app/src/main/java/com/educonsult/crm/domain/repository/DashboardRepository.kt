package com.educonsult.crm.domain.repository

import com.educonsult.crm.domain.model.DashboardStats

interface DashboardRepository {
    suspend fun getStats(): Result<DashboardStats>
}
