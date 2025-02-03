package com.sspl.ui.conference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.AttendanceResponse
import com.sspl.core.usecases.MarkAttendanceViaImageUseCase
import com.sspl.core.usecases.MarkAttendanceViaLocationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val markAttendanceViaLocationUseCase: MarkAttendanceViaLocationUseCase,
    private val markAttendanceViaImageUseCase: MarkAttendanceViaImageUseCase
) : ViewModel() {

    private val _attendanceViaLocationStatus: MutableSharedFlow<ApiStates<AttendanceResponse>> =
        MutableSharedFlow()
    val attendanceViaLocationStatus = _attendanceViaLocationStatus.asSharedFlow()

    private val _attendanceViaImageStatus: MutableSharedFlow<ApiStates<AttendanceResponse>> =
        MutableSharedFlow()
    val attendanceViaImageStatus = _attendanceViaImageStatus.asSharedFlow()


    fun markAttendanceViaLocation(
        conferenceId: Long,
        sessionId: Long,
    ) {
        viewModelScope.launch {
            markAttendanceViaLocationUseCase(conferenceId, sessionId).collect { state ->
                _attendanceViaLocationStatus.emit(state)
            }
        }
    }

    fun markAttendanceViaImage(
        conferenceId: Long, sessionId: Long, image: ByteArray
    ) {
        viewModelScope.launch {
            markAttendanceViaImageUseCase(conferenceId, sessionId, image).collect { state ->
                _attendanceViaImageStatus.emit(state)
            }
        }
    }

}