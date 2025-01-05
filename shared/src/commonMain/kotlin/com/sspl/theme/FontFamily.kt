package com.sspl.theme

import androidx.compose.runtime.Composable
import sspl.shared.generated.resources.Res

import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import sspl.shared.generated.resources.poppins_black
import sspl.shared.generated.resources.poppins_bold
import sspl.shared.generated.resources.poppins_extra_bold
import sspl.shared.generated.resources.poppins_extra_light
import sspl.shared.generated.resources.poppins_italic
import sspl.shared.generated.resources.poppins_light
import sspl.shared.generated.resources.poppins_medium
import sspl.shared.generated.resources.poppins_regular
import sspl.shared.generated.resources.poppins_semi_bold
import sspl.shared.generated.resources.poppins_thin


/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 10/08/2024.
 * se.muhammadimran@gmail.com
 */

@Composable
fun AppFontFamily() = FontFamily(
    Font(Res.font.poppins_thin, weight = FontWeight.Thin),
    Font(Res.font.poppins_extra_light, weight = FontWeight.ExtraLight),
    Font(Res.font.poppins_light, weight = FontWeight.Light),
    Font(Res.font.poppins_regular, FontWeight.Normal),
    Font(Res.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.poppins_medium, FontWeight.Medium),
    Font(Res.font.poppins_semi_bold, FontWeight.SemiBold),
    Font(Res.font.poppins_bold, FontWeight.Bold),
    Font(Res.font.poppins_extra_bold, FontWeight.ExtraBold),
    Font(Res.font.poppins_black, FontWeight.Black)
)