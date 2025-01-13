package com.sspl.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppRoundedCornerButton(modifier: Modifier = Modifier,onClick:()->Unit,title:String,isEnabled:Boolean=true) {
    Button(
        onClick = onClick,
        enabled =isEnabled ,
        shape = RoundedCornerShape(16), colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.LightGray.copy(0.7f),
        ),
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp,vertical = 6.dp)
    ) {
        AppTextLabel(
            title, color = if(isEnabled) Color.White else Color.DarkGray, modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}
@Composable
fun AppRoundedCornerButton(modifier: Modifier = Modifier,onClick:()->Unit,isEnabled:Boolean=true,content: @Composable RowScope.() -> Unit) {
    Button(
        onClick = onClick,
        enabled = isEnabled, colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.LightGray.copy(0.7f),
        ),
        shape = RoundedCornerShape(16),
        modifier = modifier.fillMaxWidth().padding(horizontal =10.dp,vertical = 6.dp)
    ) {
        content()
    }
}