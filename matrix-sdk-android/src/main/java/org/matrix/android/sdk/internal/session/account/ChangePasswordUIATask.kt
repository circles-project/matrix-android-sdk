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

package org.matrix.android.sdk.internal.session.account

import org.matrix.android.sdk.api.auth.UIABaseAuth
import org.matrix.android.sdk.api.auth.UserInteractiveAuthInterceptor
import org.matrix.android.sdk.api.session.uia.UiaResult
import org.matrix.android.sdk.internal.auth.registration.handleUIA
import org.matrix.android.sdk.internal.network.GlobalErrorReceiver
import org.matrix.android.sdk.internal.network.executeRequest
import org.matrix.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface ChangePasswordUIATask : Task<ChangePasswordUIATask.Params, Unit> {
    data class Params(
            val logoutAllDevices: Boolean,
            val userInteractiveAuthInterceptor: UserInteractiveAuthInterceptor,
            val userAuthParam: UIABaseAuth? = null
    )
}

internal class DefaultChangePasswordUIATask @Inject constructor(
        private val accountAPI: AccountAPI,
        private val globalErrorReceiver: GlobalErrorReceiver
) : ChangePasswordUIATask {

    override suspend fun execute(params: ChangePasswordUIATask.Params) {
        val changePasswordParams = ChangePasswordUIAParams.create(params.userAuthParam, params.logoutAllDevices)
        try {
            executeRequest(globalErrorReceiver) {
                accountAPI.changePasswordUIA(changePasswordParams)
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
                throw throwable
            }
        }
    }
}
