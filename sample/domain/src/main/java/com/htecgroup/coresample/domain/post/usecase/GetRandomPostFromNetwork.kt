/*
 * Copyright 2023 HTEC Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.htecgroup.coresample.domain.post.usecase

import com.htecgroup.androidcore.domain.CoreUseCase
import com.htecgroup.androidcore.domain.IUseCase
import com.htecgroup.androidcore.domain.extension.apply
import com.htecgroup.coresample.domain.post.Post
import com.htecgroup.coresample.domain.post.PostRepository
import com.htecgroup.coresample.domain.user.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.take
import javax.inject.Inject
import kotlin.random.Random

class GetRandomPostFromNetwork @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : CoreUseCase(), IUseCase<Result<Post>> {

    override suspend fun invoke(): Result<Post> {
        val postId = Random.nextInt(from = 1, until = 100)

        val postResult = postRepository.getPostFromNetwork(postId)
        if (postResult.isFailure) return postResult
        val post = postResult.getOrNull() ?: return Result.failure(IllegalStateException("Post is null"))

        val user = userRepository.getUserFromNetwork(post.userId).getOrNull()

        return Result.success(post.copy(user = user))
    }
}
