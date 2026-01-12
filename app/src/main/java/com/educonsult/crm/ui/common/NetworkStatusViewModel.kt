package com.educonsult.crm.ui.common

import androidx.lifecycle.ViewModel
import com.educonsult.crm.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NetworkStatusViewModel @Inject constructor(
    networkMonitor: NetworkMonitor
) : ViewModel() {
    val isConnected: StateFlow<Boolean> = networkMonitor.isConnected
}
