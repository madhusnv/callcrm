package com.educonsult.crm.data.mapper

import com.educonsult.crm.data.remote.dto.dashboard.response.DashboardStatsDto
import com.educonsult.crm.domain.model.DashboardStats

fun DashboardStatsDto.toDomain(): DashboardStats {
    return DashboardStats(
        totalLeads = totalLeads,
        newLeadsToday = newLeadsToday,
        newLeadsThisWeek = newLeadsThisWeek,
        pendingFollowUps = pendingFollowUps,
        overdueFollowUps = overdueFollowUps,
        totalCallsToday = totalCallsToday,
        totalCallDurationToday = totalCallDurationToday
    )
}
