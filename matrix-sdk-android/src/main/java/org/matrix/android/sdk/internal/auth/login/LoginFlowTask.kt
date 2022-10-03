/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
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

package org.matrix.android.sdk.internal.auth.login

import org.matrix.android.sdk.api.auth.data.Credentials
import org.matrix.android.sdk.api.failure.Failure
import org.matrix.android.sdk.api.failure.toRegistrationFlowResponse
import org.matrix.android.sdk.internal.auth.AuthAPI
import org.matrix.android.sdk.internal.auth.registration.RegisterTask
import org.matrix.android.sdk.internal.auth.registration.RegistrationParams
import org.matrix.android.sdk.internal.network.executeRequest
import org.matrix.android.sdk.internal.task.Task

internal interface LoginFlowTask : Task<LoginFlowTask.Params, Credentials> {
    data class Params(
            val loginParams: LoginFlowParams
    )
}

internal class DefaultLoginFlowTask(
        private val authAPI: AuthAPI
) : LoginFlowTask {

    override suspend fun execute(params: LoginFlowTask.Params): Credentials {
        try {
            return executeRequest(null) {
                authAPI.login(params.loginParams)
            }
        } catch (throwable: Throwable) {
            throw throwable.toRegistrationFlowResponse()
                    ?.let { Failure.RegistrationFlowError(it) }
                    ?: throwable
        }
    }
}
