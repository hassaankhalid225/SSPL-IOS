package com.sspl.ui.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.ApiStates
import com.sspl.resources.Res
import com.sspl.resources.demo1
import com.sspl.resources.demo2
import com.sspl.resources.demo3
import com.sspl.resources.demo4
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSmall
import com.sspl.ui.components.Error
import com.sspl.ui.components.FullScreenProgressIndicator
import com.sspl.ui.conferencescreen.LoadingScreen
import com.sspl.utils.dummyPostList
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PostDetailScreen(
    navController: NavController, viewModel: PostDetailsViewModel = koinViewModel()
) {

    val postId = navController.previousBackStackEntry?.savedStateHandle?.get<Long>(
        "postId"
    )
    LaunchedEffect(Unit) {
        postId?.let { viewModel.getPostDetails(it) }
    }
    val postDetails by viewModel.postDetails.collectAsStateWithLifecycle()

    when (postDetails) {
        is ApiStates.Failure -> {
            Error(
                modifier = Modifier.fillMaxSize(), error = (postDetails as ApiStates.Failure).error
            )
        }

        ApiStates.Loading -> {
            LoadingScreen()
        }

        is ApiStates.Success -> {
            (postDetails as ApiStates.Success<Boolean>).data?.let {
                PostDetailContent(postId = postId?:1)
            }
        }
    }
}

@Composable
fun PostDetailContent(modifier: Modifier = Modifier,postId:Long=1) {
    var comments by remember { mutableStateOf(sampleComments) }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PostItem(post = dummyPostList[postId.toInt()],
            onLikePost = { Unit }, elevation = 0.dp, cardCornerPercentage = 0,
            onDislikePost = {   },
            onCommentPost = {   }, imageAspectRatio = 16f / 9f,
            onItemClick = {   })
        AppTextLabel("Comments", modifier = Modifier.padding(horizontal = 8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) {
            items(comments) { comment ->
                CommentItem(comment)
                 HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        CommentEditor(onAddComment = { newCommentContent ->
            val newComment = Comment(
                userName = "Current User",
                userAvatar = Res.drawable.demo4,
                content = newCommentContent,
                timeAgo = "Just now",
                likes = 0
            )
            comments = comments + newComment
        })

    }


}



@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(Res.drawable.demo1),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(44.dp).aspectRatio(1f).clip(CircleShape)
        )
        Column(
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        ) {
            AppTextLabel(
                text = comment.userName,
            )
            AppTextBody(
                text = comment.content,
            )
        }
    }
}

@Composable
fun CommentEditor(onAddComment: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }
val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier.fillMaxWidth().imePadding().padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(value = commentText,
            onValueChange = { commentText = it },
            placeholder = { AppTextBody("Write a comment...") },
            modifier = Modifier.weight(1f),
            maxLines = 3
        )
        IconButton(onClick = {
            if (commentText.isNotBlank()) {
                onAddComment(commentText)
                commentText = ""
                keyboardController?.hide()
            }
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send Comment"
            )
        }
    }
}

data class Comment(
    val userName: String,
    val userAvatar: DrawableResource,
    val content: String,
    val timeAgo: String,
    val likes: Int
)




var sampleComments = listOf(
    Comment(
        userName = "John Doe",
        userAvatar = Res.drawable.demo1,
        content = "This is a great post! Thanks for sharing.",
        timeAgo = "2h ago",
        likes = 5
    ), Comment(
        userName = "Jane Smith",
        userAvatar = Res.drawable.demo2,
        content = "I have a question about this. Can you elaborate more on the second point?",
        timeAgo = "1h ago",
        likes = 2
    ), Comment(
        userName = "Bob Johnson",
        userAvatar = Res.drawable.demo3,
        content = "Interesting perspective. I hadn't thought about it that way before.",
        timeAgo = "30m ago",
        likes = 0
    )
)
