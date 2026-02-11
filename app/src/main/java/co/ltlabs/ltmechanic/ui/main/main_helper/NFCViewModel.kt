package co.ltlabs.ltmechanic.ui.main.main_helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NFCViewModel @Inject constructor() : ViewModel() {

    private var _nfcAction: NFCAction = NFCAction.NONE
    private var _action: MutableSharedFlow<NFCAction> = MutableSharedFlow()
    val nfcAction: SharedFlow<NFCAction> = _action

    private val _refreshDashboard: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val refreshDashboard: SharedFlow<Boolean> = _refreshDashboard

    // for line status
    var needMfgLineId: Long = 0
    var needMfgLine: String = ""
    var needStation: String = ""

    // for replace machine
    var mfgLineId: Long = 0
    var mfgLine: String = ""
    var machineId: Long = 0
    var machine: String = ""
    var station: String = ""

    fun setNFCAction(action: NFCAction) {
        viewModelScope.launch {
            isObserveOutsideMainActivity = false
            _nfcAction = action
            _action.emit(action)
        }
    }

    fun getNCFAction() = _nfcAction

    var isObserveOutsideMainActivity = false
    private var _scanRfid: MutableSharedFlow<String> = MutableSharedFlow()
    val scanRfid: SharedFlow<String> = _scanRfid

    fun setScanRfid(rfid: String) {
        viewModelScope.launch {
            isObserveOutsideMainActivity = false
            _scanRfid.emit(rfid)
        }
    }
}