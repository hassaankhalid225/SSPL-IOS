package com.sspl.core.models.data

import com.sspl.core.models.Post
import com.sspl.core.models.dummyPostList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PostRepositoryImpl(

) : PostsRepository {
    override suspend fun getPosts(): Flow<List<Post>> {
        return flow {
            delay(1500) // Simulating network delay for demonstration purposes
          emit(dummyPostList)
//            emit(emptyList())
        }
    }

}