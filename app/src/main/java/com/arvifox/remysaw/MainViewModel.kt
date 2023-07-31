package com.arvifox.remysaw

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RemyState(
    val url: String,
)

class MainViewModel() : ViewModel() {

    private val _remyState = MutableStateFlow(
        RemyState(
            url = "",
        )
    )
    val remyState: StateFlow<RemyState> = _remyState.asStateFlow()

    fun onUrlChanged(value: String) {
        _remyState.value = _remyState.value.copy(url = value)
    }

    fun startService(c: Context) {
        RemyWorker.start(c)
    }
}
