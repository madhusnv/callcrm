package com.educonsult.crm.di

import com.educonsult.crm.data.repository.AuthRepositoryImpl
import com.educonsult.crm.data.repository.CallRepositoryImpl
import com.educonsult.crm.data.repository.DashboardRepositoryImpl
import com.educonsult.crm.data.repository.EducationRepositoryImpl
import com.educonsult.crm.data.repository.EmployeeRepositoryImpl
import com.educonsult.crm.data.repository.LeadRepositoryImpl
import com.educonsult.crm.data.repository.TemplateRepositoryImpl
import com.educonsult.crm.domain.repository.AuthRepository
import com.educonsult.crm.domain.repository.CallRepository
import com.educonsult.crm.domain.repository.DashboardRepository
import com.educonsult.crm.domain.repository.EducationRepository
import com.educonsult.crm.domain.repository.EmployeeRepository
import com.educonsult.crm.domain.repository.LeadRepository
import com.educonsult.crm.domain.repository.TemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLeadRepository(
        leadRepositoryImpl: LeadRepositoryImpl
    ): LeadRepository

    @Binds
    @Singleton
    abstract fun bindCallRepository(
        callRepositoryImpl: CallRepositoryImpl
    ): CallRepository

    @Binds
    @Singleton
    abstract fun bindEducationRepository(
        educationRepositoryImpl: EducationRepositoryImpl
    ): EducationRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindEmployeeRepository(
        employeeRepositoryImpl: EmployeeRepositoryImpl
    ): EmployeeRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(
        templateRepositoryImpl: TemplateRepositoryImpl
    ): TemplateRepository
}
