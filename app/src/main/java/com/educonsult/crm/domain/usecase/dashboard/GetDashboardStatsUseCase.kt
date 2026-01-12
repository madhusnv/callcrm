package com.educonsult.crm.domain.usecase.dashboard

import com.educonsult.crm.domain.model.DashboardStats
import com.educonsult.crm.domain.repository.DashboardRepository
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(): Result<DashboardStats> {
        return dashboardRepository.getStats()
    }
}
