package com.sspl.ui.homescreen

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 30/01/2025.
 * se.muhammadimran@gmail.com
 */
class HomeScreenViewModel : ViewModel() {
    private val _isAdShown = MutableStateFlow<Boolean>(false)
    val isAdShown = _isAdShown.asStateFlow()

    init {
        viewModelScope.launch {
            delay(2000L)
            updateAdShown(true)
        }
    }

    fun updateAdShown(showAd: Boolean) {
        viewModelScope.launch {
            _isAdShown.update { showAd }
        }
    }

}