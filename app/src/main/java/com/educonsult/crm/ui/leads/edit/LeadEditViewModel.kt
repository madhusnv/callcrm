package com.educonsult.crm.ui.leads.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.model.LeadPriority
import com.educonsult.crm.domain.model.LeadStatus
import com.educonsult.crm.domain.usecase.lead.GetLeadByIdUseCase
import com.educonsult.crm.domain.usecase.lead.GetLeadStatusesUseCase
import com.educonsult.crm.domain.usecase.lead.SaveLeadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LeadEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLeadByIdUseCase: GetLeadByIdUseCase,
    private val saveLeadUseCase: SaveLeadUseCase,
    private val getLeadStatusesUseCase: GetLeadStatusesUseCase
) : ViewModel() {

    private val leadId: String? = savedStateHandle.get<String>("leadId")?.takeIf { it != "new" }
    val isEditMode = leadId != null

    private val _uiState = MutableStateFlow(LeadEditUiState())
    val uiState: StateFlow<LeadEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<LeadEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadStatuses()
        if (leadId != null) {
            loadLead(leadId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadStatuses() {
        getLeadStatusesUseCase()
            .onEach { statuses ->
                _uiState.update { state ->
                    state.copy(
                        statuses = statuses,
                        form = if (state.form.statusId == null && statuses.isNotEmpty()) {
                            state.form.copy(statusId = statuses.find { it.isDefault }?.id ?: statuses.first().id)
                        } else {
                            state.form
                        }
                    )
                }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun loadLead(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getLeadByIdUseCase(id)
                .onSuccess { lead ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            form = LeadFormState.fromLead(lead)
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LeadEditEvent.ShowError(e.message ?: "Failed to load lead"))
                }
        }
    }

    fun updateForm(form: LeadFormState) {
        _uiState.update { it.copy(form = form, errors = emptyMap()) }
    }

    fun updateFirstName(value: String) {
        _uiState.update {
            it.copy(
                form = it.form.copy(firstName = value),
                errors = it.errors - "firstName"
            )
        }
    }

    fun updateLastName(value: String) {
        _uiState.update { it.copy(form = it.form.copy(lastName = value)) }
    }

    fun updatePhone(value: String) {
        _uiState.update {
            it.copy(
                form = it.form.copy(phone = value),
                errors = it.errors - "phone"
            )
        }
    }

    fun updateSecondaryPhone(value: String) {
        _uiState.update { it.copy(form = it.form.copy(secondaryPhone = value)) }
    }

    fun updateEmail(value: String) {
        _uiState.update {
            it.copy(
                form = it.form.copy(email = value),
                errors = it.errors - "email"
            )
        }
    }

    fun updateStudentName(value: String) {
        _uiState.update { it.copy(form = it.form.copy(studentName = value)) }
    }

    fun updateParentName(value: String) {
        _uiState.update { it.copy(form = it.form.copy(parentName = value)) }
    }

    fun updateRelationship(value: String) {
        _uiState.update { it.copy(form = it.form.copy(relationship = value)) }
    }

    fun updateDateOfBirth(value: LocalDate?) {
        _uiState.update { it.copy(form = it.form.copy(dateOfBirth = value)) }
    }

    fun updateCurrentEducation(value: String) {
        _uiState.update { it.copy(form = it.form.copy(currentEducation = value)) }
    }

    fun updateCurrentInstitution(value: String) {
        _uiState.update { it.copy(form = it.form.copy(currentInstitution = value)) }
    }

    fun updatePercentage(value: String) {
        val percentage = value.toFloatOrNull()
        _uiState.update { it.copy(form = it.form.copy(percentage = percentage)) }
    }

    fun updateStream(value: String) {
        _uiState.update { it.copy(form = it.form.copy(stream = value)) }
    }

    fun updateGraduationYear(value: String) {
        val year = value.toIntOrNull()
        _uiState.update { it.copy(form = it.form.copy(graduationYear = year)) }
    }

    fun updateInterestedCourses(courses: List<String>) {
        _uiState.update { it.copy(form = it.form.copy(interestedCourses = courses)) }
    }

    fun updatePreferredCountries(countries: List<String>) {
        _uiState.update { it.copy(form = it.form.copy(preferredCountries = countries)) }
    }

    fun updatePreferredInstitutions(institutions: List<String>) {
        _uiState.update { it.copy(form = it.form.copy(preferredInstitutions = institutions)) }
    }

    fun updateBudgetMin(value: String) {
        val budget = value.toLongOrNull()
        _uiState.update { it.copy(form = it.form.copy(budgetMin = budget)) }
    }

    fun updateBudgetMax(value: String) {
        val budget = value.toLongOrNull()
        _uiState.update { it.copy(form = it.form.copy(budgetMax = budget)) }
    }

    fun updateIntakePreference(value: String) {
        _uiState.update { it.copy(form = it.form.copy(intakePreference = value)) }
    }

    fun updateStatusId(value: String?) {
        _uiState.update { it.copy(form = it.form.copy(statusId = value)) }
    }

    fun updatePriority(value: LeadPriority) {
        _uiState.update { it.copy(form = it.form.copy(priority = value)) }
    }

    fun updateSource(value: String) {
        _uiState.update { it.copy(form = it.form.copy(source = value)) }
    }

    fun updateNextFollowUpDate(value: LocalDateTime?) {
        _uiState.update { it.copy(form = it.form.copy(nextFollowUpDate = value)) }
    }

    fun updateReminderNote(value: String) {
        _uiState.update { it.copy(form = it.form.copy(reminderNote = value)) }
    }

    fun save() {
        val form = _uiState.value.form
        val errors = mutableMapOf<String, String>()

        if (form.firstName.isBlank()) {
            errors["firstName"] = "First name is required"
        }
        if (form.phone.isBlank()) {
            errors["phone"] = "Phone number is required"
        } else if (!isValidPhone(form.phone)) {
            errors["phone"] = "Invalid phone number"
        }
        if (form.email.isNotBlank() && !isValidEmail(form.email)) {
            errors["email"] = "Invalid email address"
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val status = _uiState.value.statuses.find { it.id == form.statusId }
            val lead = form.toLead(
                existingId = leadId,
                status = status
            )

            saveLeadUseCase(lead)
                .onSuccess { savedLead ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(LeadEditEvent.LeadSaved(savedLead.id))
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(LeadEditEvent.ShowError(e.message ?: "Failed to save lead"))
                }
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

data class LeadEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val form: LeadFormState = LeadFormState(),
    val statuses: List<LeadStatus> = emptyList(),
    val errors: Map<String, String> = emptyMap()
)

data class LeadFormState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val secondaryPhone: String = "",
    val countryCode: Int = 91,
    val email: String = "",
    val studentName: String = "",
    val parentName: String = "",
    val relationship: String = "",
    val dateOfBirth: LocalDate? = null,
    val currentEducation: String = "",
    val currentInstitution: String = "",
    val percentage: Float? = null,
    val stream: String = "",
    val graduationYear: Int? = null,
    val interestedCourses: List<String> = emptyList(),
    val preferredCountries: List<String> = emptyList(),
    val preferredInstitutions: List<String> = emptyList(),
    val budgetMin: Long? = null,
    val budgetMax: Long? = null,
    val intakePreference: String = "",
    val statusId: String? = null,
    val priority: LeadPriority = LeadPriority.MEDIUM,
    val source: String = "",
    val nextFollowUpDate: LocalDateTime? = null,
    val reminderNote: String = ""
) {
    companion object {
        fun fromLead(lead: Lead): LeadFormState {
            return LeadFormState(
                firstName = lead.firstName,
                lastName = lead.lastName ?: "",
                phone = lead.phone,
                secondaryPhone = lead.secondaryPhone ?: "",
                countryCode = lead.countryCode,
                email = lead.email ?: "",
                studentName = lead.studentName ?: "",
                parentName = lead.parentName ?: "",
                relationship = lead.relationship ?: "",
                dateOfBirth = lead.dateOfBirth,
                currentEducation = lead.currentEducation ?: "",
                currentInstitution = lead.currentInstitution ?: "",
                percentage = lead.percentage,
                stream = lead.stream ?: "",
                graduationYear = lead.graduationYear,
                interestedCourses = lead.interestedCourses,
                preferredCountries = lead.preferredCountries,
                preferredInstitutions = lead.preferredInstitutions,
                budgetMin = lead.budgetMin,
                budgetMax = lead.budgetMax,
                intakePreference = lead.intakePreference ?: "",
                statusId = lead.status?.id,
                priority = lead.priority,
                source = lead.source ?: "",
                nextFollowUpDate = lead.nextFollowUpDate,
                reminderNote = lead.reminderNote ?: ""
            )
        }
    }

    fun toLead(existingId: String?, status: LeadStatus?): Lead {
        val now = LocalDateTime.now()
        return Lead(
            id = existingId ?: UUID.randomUUID().toString(),
            firstName = firstName.trim(),
            lastName = lastName.trim().ifBlank { null },
            phone = phone.trim(),
            secondaryPhone = secondaryPhone.trim().ifBlank { null },
            countryCode = countryCode,
            email = email.trim().ifBlank { null },
            studentName = studentName.trim().ifBlank { null },
            parentName = parentName.trim().ifBlank { null },
            relationship = relationship.trim().ifBlank { null },
            dateOfBirth = dateOfBirth,
            currentEducation = currentEducation.trim().ifBlank { null },
            currentInstitution = currentInstitution.trim().ifBlank { null },
            percentage = percentage,
            stream = stream.trim().ifBlank { null },
            graduationYear = graduationYear,
            interestedCourses = interestedCourses.filter { it.isNotBlank() },
            preferredCountries = preferredCountries.filter { it.isNotBlank() },
            preferredInstitutions = preferredInstitutions.filter { it.isNotBlank() },
            budgetMin = budgetMin,
            budgetMax = budgetMax,
            intakePreference = intakePreference.trim().ifBlank { null },
            status = status,
            priority = priority,
            source = source.trim().ifBlank { null },
            assignedTo = null,
            branchId = null,
            lastContactDate = null,
            nextFollowUpDate = nextFollowUpDate,
            reminderNote = reminderNote.trim().ifBlank { null },
            totalCalls = 0,
            totalNotes = 0,
            createdAt = now,
            updatedAt = now
        )
    }
}

sealed class LeadEditEvent {
    data class ShowError(val message: String) : LeadEditEvent()
    data class LeadSaved(val leadId: String) : LeadEditEvent()
}
