package com.sspl.ui.userdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.User
import com.sspl.resources.Res
import com.sspl.resources.demo1
import com.sspl.resources.sign_out
import com.sspl.theme.orange
import com.sspl.ui.components.AppRoundedCornerButton
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextTitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel?=null,
    onParentGraph: () -> Unit = {}
) {
    val user by viewModel!!.user.collectAsStateWithLifecycle()


    ProfileScreenContent(
        onSignOut = {
            viewModel?.onSignOut()
            onParentGraph()
        },
        user = user
    )


}

@Composable
fun ProfileScreenContent(onSignOut: () -> Unit, user: User?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.1f))
        ProfileImage()
        Spacer(Modifier.height(20.dp))

        BasicInformationSection(user)
        Spacer(Modifier.height(16.dp))
        PersonalDetailsSection()
        Spacer(Modifier.weight(1f))
        SignOutButton(onSignOut)
    }
}

@Composable
private fun ProfileImage(onChangeProfilePicture: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(130.dp)
            .aspectRatio(1f)
    ) {
        Image(
            painter = painterResource(Res.drawable.demo1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )

        Box(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)

        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit profile picture",
                tint = Color.White,
                modifier = Modifier.fillMaxSize().clickable(onClick = onChangeProfilePicture)
                    .padding(8.dp)
            )
        }
    }
}


@Composable
private fun BasicInformationSection(user: User?) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Spacer(Modifier.height(4.dp))
        InfoCard {
            SectionTitle("Personal Details")
            InfoRow("Name", user?.firstName + " " + user?.lastName)
            InfoRow("Email", user?.email ?: "")
            InfoRow("Phone", user?.phone?.toString() ?: "")
        }
    }
}

@Composable
private fun PersonalDetailsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(4.dp))
        InfoCard {
            SectionTitle("Professional Details")
            InfoRow("PMDC No", "17BS25054")
            InfoRow("Institute", "Institute Of Pakistan")
            InfoRow("City", "Lahore")
            InfoRow("Country", "Pakistan")
            InfoRow("Designation", "Software Engineer")
            InfoRow("Registration No", "34673-43AD3")
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    AppTextTitle(
        text = title,
        fontSize = 14.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SignOutButton(onSignOut: () -> Unit) {
    AppRoundedCornerButton(
        onClick = onSignOut,
        title = stringResource(Res.string.sign_out)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AppTextLabel(
            text = "$label:",
        )
        AppTextBody(
            text = value,
        )
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

