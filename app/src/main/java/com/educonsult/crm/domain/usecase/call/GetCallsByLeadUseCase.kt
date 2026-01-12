package com.educonsult.crm.domain.usecase.call

import com.educonsult.crm.data.local.db.entity.CallLogEntity
import com.educonsult.crm.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallsByLeadUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    operator fun invoke(leadId: String): Flow<List<CallLogEntity>> {
        return callRepository.getCallLogsByLead(leadId)
    }
}
