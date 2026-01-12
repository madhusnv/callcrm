package com.educonsult.crm.ui.courses.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educonsult.crm.domain.model.Country
import com.educonsult.crm.domain.model.Course
import com.educonsult.crm.domain.model.Institution
import com.educonsult.crm.domain.usecase.education.GetCountriesUseCase
import com.educonsult.crm.domain.usecase.education.GetCoursesUseCase
import com.educonsult.crm.domain.usecase.education.GetInstitutionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val countries: List<Country> = emptyList(),
    val institutions: List<Institution> = emptyList(),
    val courses: List<Course> = emptyList(),
    val selectedCountryId: String? = null,
    val selectedInstitutionId: String? = null,
    val selectedLevel: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val getInstitutionsUseCase: GetInstitutionsUseCase,
    private val getCoursesUseCase: GetCoursesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseListUiState())
    val uiState: StateFlow<CourseListUiState> = _uiState.asStateFlow()

    init {
        loadCountries()
        loadCourses()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun applySearch() {
        loadCourses()
    }

    fun selectCountry(countryId: String?) {
        _uiState.update {
            it.copy(
                selectedCountryId = countryId,
                selectedInstitutionId = null
            )
        }
        loadInstitutions()
        loadCourses()
    }

    fun selectInstitution(institutionId: String?) {
        _uiState.update { it.copy(selectedInstitutionId = institutionId) }
        loadCourses()
    }

    fun selectLevel(level: String?) {
        _uiState.update { it.copy(selectedLevel = level) }
        loadCourses()
    }

    fun refresh() {
        loadCountries()
        loadInstitutions()
        loadCourses()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getCountriesUseCase()
            result.onSuccess { countries ->
                _uiState.update { it.copy(countries = countries, isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }

    private fun loadInstitutions() {
        val countryId = _uiState.value.selectedCountryId
        viewModelScope.launch {
            val result = getInstitutionsUseCase(countryId = countryId)
            result.onSuccess { institutions ->
                _uiState.update { it.copy(institutions = institutions) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    private fun loadCourses() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getCoursesUseCase(
                countryId = state.selectedCountryId,
                institutionId = state.selectedInstitutionId,
                level = state.selectedLevel,
                search = state.searchQuery.takeIf { it.isNotBlank() }
            )
            result.onSuccess { courses ->
                _uiState.update { it.copy(courses = courses, isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }
}
