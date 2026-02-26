package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MfgLine
import javax.inject.Inject

private const val TAG = "SetupLineViewModel";

class SetupLineSelectLineViewModel @Inject constructor() : ViewModel() {

    private val _navigateToSelectedLine = MutableLiveData<MfgLine>()
    val navigateToSelectedLine: LiveData<MfgLine>
        get() = _navigateToSelectedLine

    private val _mfgLines = MutableLiveData<MutableList<MfgLine>>()
    val mfgLines: LiveData<MutableList<MfgLine>>
        get() = _mfgLines

    init {
        Log.d(TAG, "init: viewmodel is working...")

        _mfgLines.value = listOf(
            MfgLine(3, "STX02", "", 2),
            MfgLine(1, "YTI01", "",1),
            MfgLine(2, "YTI03", "",3)
        ).sortedBy { it.seq }.toMutableList()
    }

    fun displaySetupLine(mfgLine: MfgLine) {
        _navigateToSelectedLine.value = mfgLine
    }

    fun displaySetupLineComplete() {
        _navigateToSelectedLine.value = null
    }

    fun setMfgLines(mfgLines: MutableList<MfgLine>) {
        _mfgLines.value = mfgLines
    }
}