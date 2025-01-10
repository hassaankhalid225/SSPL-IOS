package com.sspl.ui.postscreen

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
import com.sspl.resources.place_holder
import org.jetbrains.compose.resources.painterResource


@Composable
fun PostItem(modifier: Modifier = Modifier, post: Post) {
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
            PostInteractions(post)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostInteractions(post: Post, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InteractionButton(icon = Icons.Filled.ThumbUp,
            count = post.likes,
            contentDescription = "Like",
            onClick = { /* Handle like action */ })
        InteractionButton(icon = Icons.Filled.ThumbUp,
            count = post.dislikes,
            contentDescription = "Dislike",
            onClick = { /* Handle dislike action */ })
        InteractionButton(icon = Icons.Filled.ThumbUp,
            count = post.commentCount,
            contentDescription = "Comment",
            onClick = { /* Handle comment action */ })
    }
}

@Composable
fun InteractionButton(
    icon: ImageVector, count: Int?, contentDescription: String, onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = Color.Gray)
        }
        Text(
            text = buildAnnotatedString {
                append(count.toString())
            }, modifier = Modifier.wrapContentWidth()
        )
    }
}


fun LazyListScope.posts(
    items: List<Post>,
) = itemsIndexed(items = items, key = { index, item -> index }, itemContent = { index, item ->
    PostItem(post = item)
})