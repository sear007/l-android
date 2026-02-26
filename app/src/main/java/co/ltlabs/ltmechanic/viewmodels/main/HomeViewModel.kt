package co.ltlabs.ltmechanic.viewmodels.main

import android.util.Log
import androidx.lifecycle.ViewModel
import javax.inject.Inject

private const val TAG = "HomeViewModel";

class HomeViewModel @Inject constructor() : ViewModel() {

    init {
        Log.d(TAG, "init: viewmodel is working...")
    }
}