package com.educonsult.crm.data.remote.dto.dashboard.response

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatsDto(
    val totalLeads: Int = 0,
    val newLeadsToday: Int = 0,
    val newLeadsThisWeek: Int = 0,
    val pendingFollowUps: Int = 0,
    val overdueFollowUps: Int = 0,
    val totalCallsToday: Int = 0,
    val totalCallDurationToday: Int = 0
)
