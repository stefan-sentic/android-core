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
import com.htecgroup.coresample.domain.post.Post
import kotlinx.coroutines.flow.lastOrNull
import javax.inject.Inject
import kotlin.random.Random

class GetRandomPost @Inject constructor(
    private val retrievePost: RetrievePost
) : CoreUseCase(), IUseCase<Result<Post>> {

    override suspend fun invoke(): Result<Post> {
        val postId = Random.nextInt(from = 1, until = 100)

        val postResult = retrievePost.invoke(postId).lastOrNull()
        return if (postResult?.getOrNull() == null) {
            Result.failure(IllegalStateException("Unknown exception. The post was null."))
        } else {
            @Suppress("UNCHECKED_CAST")
            postResult as Result<Post>
        }
    }
}
