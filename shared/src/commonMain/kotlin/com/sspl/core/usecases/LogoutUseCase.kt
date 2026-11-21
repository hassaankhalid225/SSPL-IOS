package com.sspl.core.usecases

import com.sspl.core.repositories.AuthRepository
import com.sspl.session.UserSession
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Sign out — mirrors web DashboardLayout.jsx `logout()`: best-effort POST /auth/logout
 * { refresh_token } so the server revokes the refresh token, then the local session is always
 * cleared. Network failures / timeouts never block signing out.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val userSession: UserSession
) {
    suspend operator fun invoke() {
        val refresh = userSession.refreshToken
        if (!refresh.isNullOrBlank()) {
            try {
                withTimeoutOrNull(LOGOUT_TIMEOUT_MS) { authRepository.logout(refresh) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Ignore network errors on logout (web parity).
            }
        }
        userSession.resetToken()
        userSession.resetUser()
    }

    private companion object {
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
