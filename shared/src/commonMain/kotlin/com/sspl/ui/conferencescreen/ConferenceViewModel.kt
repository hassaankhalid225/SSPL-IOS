package com.sspl.ui.conferencescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.Conference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import sspl.shared.generated.resources.Res

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 05/01/2025.
 * se.muhammadimran@gmail.com
 */
class ConferenceViewModel : ViewModel() {
    private val _conferences = MutableStateFlow<List<Conference>>(emptyList())
    val conferences = _conferences.asStateFlow()


    init {
        loadData()
    }


    @OptIn(ExperimentalResourceApi::class)
    fun loadData() {
        viewModelScope.launch(context = Dispatchers.IO) {
            val readBytes = Res.readBytes("files/conferences.json")
            val json = readBytes.decodeToString()
            val data = Json.decodeFromString<List<Conference>>(json)
            _conferences.update { data+data+data+data+data }
        }
    }
}