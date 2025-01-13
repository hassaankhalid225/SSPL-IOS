package com.sspl.core.usecases

import com.sspl.ui.kyc.ValidationResult

class ValidateUserLastNameUseCase {
    operator fun invoke(lastName: String?): ValidationResult {
        if (lastName.isNullOrEmpty()) {
            return ValidationResult.Error("Last name cannot be empty")
        }
        if (lastName.any { it.isDigit() }) {
            return ValidationResult.Error("Last name cannot contain numbers")
        }
        return ValidationResult.Success
    }
}