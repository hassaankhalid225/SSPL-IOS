package com.sspl.ui.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.resources.Res
import com.sspl.resources.demo1
import com.sspl.resources.demo2
import com.sspl.resources.ic_user
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.AppTextTitle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AboutScreen(navController: NavController, modifier: Modifier = Modifier) {

    AboutScreenContent(modifier)

}

@Composable
fun AboutScreenContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Spacer(Modifier.weight(0.05f))
        PresidentInfo()
        Spacer(Modifier.weight(0.1f))
        AppTextSubTitle("Dean's Message")
        MessageParagraph(
            modifier = Modifier.fillMaxWidth(),
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        )

        Spacer(Modifier.height(20.dp))

        AppTextSubTitle("Members")
        Spacer(Modifier.height(10.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(10) {
                MemberCard()
            }
        }
        Spacer(Modifier.weight(0.25f))

    }

}

@Composable
fun PresidentInfo(
    modifier: Modifier = Modifier,
    presidentImage: Any = Res.drawable.demo1,
    postName: String = "Dean of KE-ABC-Xyz",
    presidentName: String = "Dr President XYZ",
) {
    Row(
        modifier.fillMaxWidth().padding(end = 20.dp), verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(presidentImage as DrawableResource),
            contentDescription = null,
            modifier = Modifier.size(120.dp).aspectRatio(1f).clip(RoundedCornerShape(4)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.weight(.6f))
        Column(
            horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppTextTitle(text = presidentName, fontSize = 18.sp)
            AppTextSubTitle(text = postName, fontSize = 16.sp)
        }
        Spacer(Modifier.weight(.4f))


    }
}

@Composable
fun MessageParagraph(
    text: String,
    modifier: Modifier = Modifier,
) {
    val firstLetter = text.take(1)
    val restOfText = text.drop(1)

    Row(modifier = modifier) {
        AppTextSubTitle(
            text = firstLetter, style = TextStyle(
                fontSize = 15.sp,
            ), modifier = Modifier.alignByBaseline()
        )
        AppTextBody(
            text = restOfText, modifier = Modifier.alignByBaseline()
        )
    }
}

@Composable
fun MemberCard(
    modifier: Modifier = Modifier,
    image: Any = Res.drawable.demo2,
    name: String = "Member Name ABC",
    post: String = "Member Post XYZ"
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(image as DrawableResource),
                contentDescription = null,
                modifier = Modifier.size(120.dp, 140.dp).aspectRatio(1f)
                    .clip(RoundedCornerShape(4)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(4.dp))
            AppTextSubTitle(text = name)
            Spacer(Modifier.height(4.dp))
            AppTextSubTitle(text = post)
        }
    }
}