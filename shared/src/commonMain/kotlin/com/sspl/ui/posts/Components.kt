package com.sspl.ui.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.sspl.core.models.Post
import com.sspl.resources.Res
import com.sspl.resources.comment_outline
import com.sspl.resources.demo1
import com.sspl.resources.demo2
import com.sspl.resources.demo3
import com.sspl.resources.place_holder
import com.sspl.resources.thumb_down_outline
import com.sspl.resources.thumb_up_outline
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextSmall
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.AppTextTitle
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
                AppTextSubTitle(
                    text = post.userName ?: "",
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            post.description?.let {
                AppTextBody(
                    text = post.description
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
                        painter = painterResource(Res.drawable.demo1) /*(post.images[0])*/,
                        contentDescription = "Post image 1",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f) // Adjust this ratio as needed
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
                            painter = painterResource(Res.drawable.demo2) /*(post.images[0])*/,
                            contentDescription = "Post image 1",
                            modifier = Modifier
                                .weight(1f).aspectRatio(1f) // Square aspect ratio for side-by-side images
                                .clip(RoundedCornerShape(5)),
                            contentScale = ContentScale.Crop
                        )
                        VerticalDivider(
                            thickness = 5.dp,
                        )
                        Image(
                            painter = painterResource(Res.drawable.demo1) /*(post.images[1])*/,
                            contentDescription = "Post image 2",
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square aspect ratio for side-by-side images
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
        Spacer(Modifier.width(5.dp))
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
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Image(
                painter = painterResource(imageRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp).clickable(onClick = onClick), // Adjust size as needed
                contentScale = ContentScale.Fit // Adjust scale as needed
            )
        AppTextSmall(
            text = buildAnnotatedString {
                append(count?.toString() ?: "0")
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

