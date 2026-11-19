package com.sspl.core.auth

import com.sspl.core.models.User
import com.sspl.utils.BASE_URL

object RoleUtils {

    val MEMBER_OR_ADMIN_ROLES = setOf(
        "LIFE_MEMBER",
        "REGULAR_MEMBER",
        "SUPER_ADMIN",
        "ADMIN",
        "PRESIDENT",
        "VICE_PRESIDENT",
        "GENERAL_SECRETARY",
        "JOINT_SECRETARY",
        "FINANCE_SECRETARY",
        "PUBLICATION_SECRETARY",
        "EXECUTIVE_COUNCIL_MEMBER"
    )

    fun getProfileImageUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://") ||
            path.startsWith("data:image") || path.startsWith("blob:")
        ) {
            return path
        }
        val cleanBase = BASE_URL.trimEnd('/')
        val cleanPath = path.trimStart('/')
        return "$cleanBase/$cleanPath"
    }

    fun getUserRoles(user: User?): List<String> {
        if (user == null) return emptyList()
        if (user.roles.isNotEmpty()) {
            return user.roles.map { it.uppercase() }
        }
        return if (!user.role.isNullOrBlank()) listOf(user.role.uppercase()) else emptyList()
    }

    fun hasUserRole(user: User?, roleName: String): Boolean {
        return getUserRoles(user).contains(roleName.uppercase())
    }

    fun hasActiveMembershipPlan(user: User?): Boolean {
        if (user == null) return false
        val roles = getUserRoles(user)
        if (roles.any { it in MEMBER_OR_ADMIN_ROLES }) return true
        return user.membershipStatus.equals("ACTIVE", ignoreCase = true) &&
                (user.membershipPlan != null || (user.planId != null && user.planId > 0))
    }

    fun getUserDisplayName(user: User?): String {
        if (user == null) return "Member"
        val name = "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}".trim()
        return if (name.isNotBlank()) name else "Member"
    }

    fun getUserInitials(user: User?): String {
        if (user == null) return "M"
        val first = user.firstName?.firstOrNull()?.toString().orEmpty()
        val last = user.lastName?.firstOrNull()?.toString().orEmpty()
        val initials = "$first$last".uppercase()
        return if (initials.isNotBlank()) initials else "M"
    }

    fun getMembershipLabel(user: User?): String {
        if (user == null) return "Guest"
        if (user.membershipStatus.equals("PENDING", ignoreCase = true)) return "Pending review"

        val roles = getUserRoles(user)
        val planName = user.membershipPlan?.name ?: user.membershipPlan?.title ?: ""
        val extraBadges = mutableListOf<String>()
        if (roles.contains("FELLOWSHIP_TRAINER")) extraBadges.add("Fellowship Trainer")
        if (roles.contains("FELLOWSHIP_FELLOW")) extraBadges.add("Fellow")

        if (planName.isNotBlank() && extraBadges.isNotEmpty()) {
            return "$planName · ${extraBadges.joinToString(" · ")}"
        }
        if (planName.isNotBlank()) return planName
        if (roles.isNotEmpty() && roles.first() != "USER") {
            return roles.joinToString(" · ") { it.replace('_', ' ') }
        }
        return if (user.isGuest()) "Guest user" else "Registered user"
    }

    fun getAdminPortalBaseUrl(): String {
        return "https://admin.ssplahore.com"
    }

    fun getTrainerPortalRedirectUrl(token: String?): String {
        val base = getAdminPortalBaseUrl()
        return if (!token.isNullOrBlank()) {
            "$base/fellowship#sso=$token"
        } else {
            "$base/fellowship"
        }
    }
}

