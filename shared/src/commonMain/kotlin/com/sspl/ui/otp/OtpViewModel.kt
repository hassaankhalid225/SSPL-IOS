package com.sspl.ui.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtpViewModel :ViewModel() {
 private val _resultFlow= MutableSharedFlow<OtpOneTimeEvents>()
    val resultFlow=_resultFlow.asSharedFlow()

    private val _isVerifying= MutableStateFlow(false)
    val isVerifying=_isVerifying.asStateFlow()

    private val _otpValue= MutableStateFlow("")
    val otpValue=_otpValue.asStateFlow()

    fun verifyOtp(otp:String){
        viewModelScope.launch {
            _isVerifying.value=true
            delay(1500)
            if (otp=="123456"){
                _resultFlow.emit(OtpOneTimeEvents.Success)
            }else{
                _resultFlow.emit(OtpOneTimeEvents.ShowMessage("Invalid OTP"))
            }
            _isVerifying.value=false
        }
    }
    fun resendOtp(){
        viewModelScope.launch {
            _otpValue.value=""
            _resultFlow.emit(OtpOneTimeEvents.ShowMessage("Sent OTP"))
        }
    }
    fun onOTPValueChange(value:String){
        _otpValue.value=value
    }

}
sealed interface OtpOneTimeEvents{
    data class ShowMessage(val message:String):OtpOneTimeEvents
    data object Success:OtpOneTimeEvents
}
