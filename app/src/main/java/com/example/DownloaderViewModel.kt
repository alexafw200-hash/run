package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloaderViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady = _isEngineReady.asStateFlow()

    fun initializeEngine(context: Context) {
        viewModelScope.launch {
            val success = EngineManager.initializeEngine(context)
            _isEngineReady.value = success
            if (!success) {
                appendLog("Failed to initialize engine. Missing files in assets directory?")
            } else {
                appendLog("Engine initialized successfully.")
            }
        }
    }

    fun startDownload(context: Context, url: String) {
        if (url.isBlank()) {
            appendLog("Error: URL cannot be empty")
            return
        }
        viewModelScope.launch {
            _isDownloading.value = true
            val downloaderService = DownloaderService(context)
            
            downloaderService.downloadVideo(url).collect { logLine ->
                appendLog(logLine)
            }
            _isDownloading.value = false
        }
    }

    private fun appendLog(log: String) {
        _logs.value = _logs.value + log
    }
}
