package com.educonsult.crm.domain.usecase.call

import com.educonsult.crm.domain.repository.CallRepository
import javax.inject.Inject

class GetCallStreamUrlUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(recordingId: String): Result<String> {
        return callRepository.getStreamUrl(recordingId)
    }
}
