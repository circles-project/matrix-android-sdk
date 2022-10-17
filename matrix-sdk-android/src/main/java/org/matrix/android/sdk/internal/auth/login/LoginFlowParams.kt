package org.matrix.android.sdk.internal.auth.login

/*
 * Copyright 2021 The Matrix.org Foundation C.I.C.
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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.matrix.android.sdk.api.util.JsonDict

/**
 * Class to pass parameters to the custom login types for /login.
 */
@JsonClass(generateAdapter = true)
internal data class LoginFlowParams(
        // authentication parameters
        @Json(name = "auth")
        val auth: JsonDict? = null,

        @Json(name = "identifier")
        val identifier: JsonDict? = null,

        // device name
        @Json(name = "initial_device_display_name")
        val initialDeviceDisplayName: String? = null
)
