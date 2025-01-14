package com.sspl.ui.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sspl.core.models.Post
import com.sspl.resources.Res
import com.sspl.resources.comment_outline
import com.sspl.resources.place_holder
import com.sspl.resources.thumb_down_outline
import com.sspl.resources.thumb_up_outline
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun PostItem(
    modifier: Modifier = Modifier,
    post: Post,
    onLikePost: () -> Unit,
    onDislikePost: () -> Unit,
    onCommentPost: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = (painterResource(Res.drawable.place_holder)),
                    contentDescription = "User profile picture",
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.userName ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            post.description?.let {
                Text(
                    text = post.description ?: "", style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Images
            when {
                post.images.isNullOrEmpty() -> {
                    // No images, so we don't add any image composable
                }

                post.images.size == 1 -> {
                    Image(
                        painter = painterResource(Res.drawable.place_holder) /*(post.images[0])*/,
                        contentDescription = "Post image 1",
                        modifier = Modifier.fillMaxWidth(1f).height(200.dp)
                            .clip(RoundedCornerShape(5)),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    // Two images side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.place_holder) /*(post.images[0])*/,
                            contentDescription = "Post image 1",
                            modifier = Modifier.weight(1f).height(200.dp)
                                .clip(RoundedCornerShape(5)),
                            contentScale = ContentScale.Crop
                        )
                        VerticalDivider(
                            thickness = 5.dp,

                            )
                        Image(
                            painter = painterResource(Res.drawable.place_holder) /*(post.images[1])*/,
                            contentDescription = "Post image 2",
                            modifier = Modifier.weight(1f).height(200.dp)
                                .clip(RoundedCornerShape(5)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            PostInteractions(
                post,
                onLikePost = onLikePost,
                onDislikePost = onDislikePost,
                onCommentPost = onCommentPost
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostInteractions(
    post: Post,
    modifier: Modifier = Modifier,
    onLikePost: () -> Unit,
    onDislikePost: () -> Unit,
    onCommentPost: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InteractionButton(
            imageRes = Res.drawable.comment_outline,
            count = post.commentCount,
            contentDescription = "Comment",
            onClick = onCommentPost
        )
        Spacer(Modifier.weight(1f))

        InteractionButton(
            imageRes = Res.drawable.thumb_up_outline,
            count = post.likes,
            contentDescription = "Like",
            onClick = onLikePost
        )
        InteractionButton(
            imageRes = Res.drawable.thumb_down_outline,
            count = post.dislikes,
            contentDescription = "Dislike",
            onClick = onDislikePost
        )
    }
}


@Composable
fun InteractionButton(
    imageRes: DrawableResource, count: Int?, contentDescription: String, onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClick) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp), // Adjust size as needed
                contentScale = ContentScale.Fit // Adjust scale as needed
            )
        }
        Text(
            text = buildAnnotatedString {
                append(count?.toString() ?: "0") // Ensure count is not null
            },
            modifier = Modifier.wrapContentWidth()
        )
    }
}



fun LazyListScope.posts(
    items: List<Post>,
    onLikePost: (Long) -> Unit,
    onDislikePost: (Long) -> Unit,
    onCommentPost: (Long) -> Unit,
    modifier: Modifier = Modifier
) = itemsIndexed(items = items, key = { index, _ -> index }, itemContent = { _, item ->
    PostItem(
        post = item,
        modifier = modifier,
        onLikePost = { onLikePost(item.postId) },
        onDislikePost = { onDislikePost(item.postId) },
        onCommentPost = { onCommentPost(item.postId) }
    )
})

