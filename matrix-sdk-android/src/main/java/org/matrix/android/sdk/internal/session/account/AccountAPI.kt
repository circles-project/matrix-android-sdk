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

import org.matrix.android.sdk.internal.network.NetworkConstants
import retrofit2.http.Body
import retrofit2.http.POST

internal interface AccountAPI {

    /**
     * Ask the homeserver to change the password with the provided new password.
     * @param params parameters to change password.
     */
    @POST(NetworkConstants.URI_API_PREFIX_PATH_R0 + "account/password")
    suspend fun changePassword(@Body params: ChangePasswordParams)

    /**
     * Deactivate the user account.
     *
     * @param params the deactivate account params
     */
    @POST(NetworkConstants.URI_API_PREFIX_PATH_R0 + "account/deactivate")
    suspend fun deactivate(@Body params: DeactivateAccountParams)

    //Added to handle change password uia stages
    @POST(NetworkConstants.URI_API_PREFIX_PATH_V3 + "account/auth/password")
    suspend fun changePasswordUIA(@Body params: AuthUIAParams)

    //Added to handle forgot password uia stages
    @POST(NetworkConstants.URI_API_PREFIX_PATH_V3 + "account/auth/recovery")
    suspend fun resetPasswordUIA(@Body params: AuthUIAParams)

    //Added to handle change email uia stages
    @POST(NetworkConstants.URI_API_PREFIX_PATH_V3 + "account/auth/email")
    suspend fun changeEmailUIA(@Body params: AuthUIAParams)
}
