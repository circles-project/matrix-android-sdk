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

package org.matrix.android.sdk.internal.session.identity.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Just to consider common parameters
private interface IdentityRequestTokenBody {
    /**
     * Required. A unique string generated by the client, and used to identify the validation attempt.
     * It must be a string consisting of the characters [0-9a-zA-Z.=_-].
     * Its length must not exceed 255 characters and it must not be empty.
     */
    val clientSecret: String

    val sendAttempt: Int
}

@JsonClass(generateAdapter = true)
internal data class IdentityRequestTokenForEmailBody(
        @Json(name = "client_secret")
        override val clientSecret: String,

        /**
         * Required. The server will only send an email if the send_attempt is a number greater than the most
         * recent one which it has seen, scoped to that email + client_secret pair. This is to avoid repeatedly
         * sending the same email in the case of request retries between the POSTing user and the identity server.
         * The client should increment this value if they desire a new email (e.g. a reminder) to be sent.
         * If they do not, the server should respond with success but not resend the email.
         */
        @Json(name = "send_attempt")
        override val sendAttempt: Int,

        /**
         * Required. The email address to validate.
         */
        @Json(name = "email")
        val email: String
) : IdentityRequestTokenBody

@JsonClass(generateAdapter = true)
internal data class IdentityRequestTokenForMsisdnBody(
        @Json(name = "client_secret")
        override val clientSecret: String,

        /**
         * Required. The server will only send an SMS if the send_attempt is a number greater than the most recent one
         * which it has seen, scoped to that country + phone_number + client_secret triple. This is to avoid repeatedly
         * sending the same SMS in the case of request retries between the POSTing user and the identity server.
         * The client should increment this value if they desire a new SMS (e.g. a reminder) to be sent.
         */
        @Json(name = "send_attempt")
        override val sendAttempt: Int,

        /**
         * Required. The phone number to validate.
         */
        @Json(name = "phone_number")
        val phoneNumber: String,

        /**
         * Required. The two-letter uppercase ISO-3166-1 alpha-2 country code that the number in phone_number
         * should be parsed as if it were dialled from.
         */
        @Json(name = "country")
        val countryCode: String
) : IdentityRequestTokenBody
