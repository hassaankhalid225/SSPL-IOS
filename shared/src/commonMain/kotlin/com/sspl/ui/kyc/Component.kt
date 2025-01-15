package com.sspl.ui.kyc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.resources.Res
import com.sspl.resources.create_an_account_so_you_can_interact_seamlessly
import com.sspl.resources.create_new_account
import com.sspl.theme.primary
import com.sspl.ui.components.AppRoundedCornerButton
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextTitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun HeaderSection(
    title: String = stringResource(Res.string.create_new_account),
    subTitle: String = stringResource(Res.string.create_an_account_so_you_can_interact_seamlessly)
) {
    AppTextTitle(
        text = title, fontSize = 26.sp, color = primary
    )
    AppTextTitle(
        text = subTitle,
        textAlign = TextAlign.Center
    )

}


