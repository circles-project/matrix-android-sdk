/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.android.sdk.internal.session.profile

import org.matrix.android.sdk.api.auth.UIABaseAuth
import org.matrix.android.sdk.api.auth.UserInteractiveAuthInterceptor
import org.matrix.android.sdk.api.failure.Failure
import org.matrix.android.sdk.api.failure.toRegistrationFlowResponse
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.uia.UiaResult
import org.matrix.android.sdk.internal.auth.registration.handleUIA
import org.matrix.android.sdk.internal.network.GlobalErrorReceiver
import org.matrix.android.sdk.internal.network.executeRequest
import org.matrix.android.sdk.internal.task.Task
import timber.log.Timber
import javax.inject.Inject

//Created for Circles
internal abstract class DeleteEmailThreePidUIATask : Task<DeleteEmailThreePidUIATask.Params, Unit> {
    data class Params(
            val email: String,
            val userInteractiveAuthInterceptor: UserInteractiveAuthInterceptor,
            val userAuthParam: UIABaseAuth? = null
    )
}

//Created for Circles
internal class DefaultDeleteEmailThreePidUIATask @Inject constructor(
        private val profileAPI: ProfileAPI,
        private val globalErrorReceiver: GlobalErrorReceiver
) : DeleteEmailThreePidUIATask() {

    override suspend fun execute(params: Params) {
        val body = DeleteEmailThreePidUIABody.create(params.userAuthParam, ThreePid.Email(params.email))
        try {
            executeRequest(globalErrorReceiver) {
                profileAPI.deleteEmailThreePidUIA(body)
            }
        } catch (throwable: Throwable) {
            if (handleUIA(
                            failure = throwable,
                            interceptor = params.userInteractiveAuthInterceptor,
                            retryBlock = { authUpdate ->
                                execute(params.copy(userAuthParam = authUpdate))
                            }
                    ) != UiaResult.SUCCESS
            ) {
                Timber.d("## UIA: propagate failure")
                throw throwable.toRegistrationFlowResponse()
                        ?.let { Failure.RegistrationFlowError(it) }
                        ?: throwable
            }
        }
    }
}
