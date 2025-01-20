package com.sspl.core.models

import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Resource("auth/signup")
class SignUpRequest()


@Serializable
@Resource("auth/login")
class SignInRequest()



