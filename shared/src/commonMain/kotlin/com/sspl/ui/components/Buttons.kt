package com.sspl.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.resources.Res
import com.sspl.resources.sign_in_via_email
import com.sspl.resources.sign_in_via_phone
import com.sspl.ui.kyc.LoginScreenUiEvents
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppRoundedCornerButton(modifier: Modifier = Modifier,onClick:()->Unit,title:String) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16),
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        AppTextBody(
            title,
            fontSize = 12.sp, color = Color.White
        )
    }
}
@Composable
fun AppRoundedCornerButton(modifier: Modifier = Modifier,onClick:()->Unit,content: @Composable RowScope.() -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16),
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        content()
    }
}