package com.sspl.core.usecases


class SignInUseCase(
    private val emailValidationUseCase: EmailValidationUseCase,
    private val passwordValidationUseCase: PasswordValidationUseCase,
    private val phoneNumberValidationUseCase: PhoneNumberValidationUseCase
) {
    fun validateEmailAndPassword(email: String, password: String): Pair<Boolean, Boolean> {
        val isEmailValid = emailValidationUseCase(email)
        val isPasswordValid = passwordValidationUseCase(password)
        return isEmailValid to isPasswordValid
    }

    fun validatePhoneNumber(phoneNumber: String): Boolean {
        return phoneNumberValidationUseCase(phoneNumber)
    }
}