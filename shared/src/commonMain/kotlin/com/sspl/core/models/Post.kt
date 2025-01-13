package com.sspl.core.models

data class Post(
    val postId: Long,
    val userName: String?,
    val userProfileUrl: String?,
    val description: String?,
    val images: List<String>?,
    val likes: Int?,
    val dislikes: Int?,
    val commentCount: Int?,
    val disLikeCount: Int?,
)

val dummyPostList = listOf(
    Post(
        postId = 1,
        userName = "JohnDoe",
        userProfileUrl = "https://example.com/john_doe.jpg",
        description = "Enjoying a beautiful day!",
        images = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
        likes = 100,
        dislikes = 5,
        commentCount = 20,
        disLikeCount = 5
    ),
    Post(
        postId = 2,
        userName = "JaneSmith",
        userProfileUrl = "https://example.com/jane_smith.jpg",
        description = "Weekend vibes!",
        images = listOf("https://example.com/image3.jpg"),
        likes = 50,
        dislikes = 2,
        commentCount = 15,
        disLikeCount = 2
    ),
    Post(
        postId = 3,
        userName = "JaneSmith",
        userProfileUrl = "https://example.com/jane_smith.jpg",
        description = null,
        images = listOf("https://example.com/image3.jpg"),
        likes = 50,
        dislikes = 2,
        commentCount = 15,
        disLikeCount = 2
    ),
    Post(
        postId =4,
        userName = "AwesomeUser",
        userProfileUrl = "https://example.com/awesome_user.jpg",
        description = "Just chilling...",
        images = emptyList(),
        likes = 200,
        dislikes = 10,
        commentCount = 30,
        disLikeCount = 10
    )
)