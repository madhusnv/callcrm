package com.educonsult.crm.domain.model

data class DashboardStats(
    val totalLeads: Int,
    val newLeadsToday: Int,
    val newLeadsThisWeek: Int,
    val pendingFollowUps: Int,
    val overdueFollowUps: Int,
    val totalCallsToday: Int,
    val totalCallDurationToday: Int
)
