/*
 * Copyright (c) 2023 The Matrix.org Foundation C.I.C.
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
package org.matrix.android.sdk.internal.database.migration

import io.realm.DynamicRealm
import org.matrix.android.sdk.internal.database.model.EventEntityFields
import org.matrix.android.sdk.internal.database.model.EventInsertEntityFields
import org.matrix.android.sdk.internal.database.model.LocalRoomSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.PushRulesEntityFields
import org.matrix.android.sdk.internal.database.model.PusherEntityFields
import org.matrix.android.sdk.internal.database.model.RoomEntityFields
import org.matrix.android.sdk.internal.database.model.RoomMemberSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.RoomSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.presence.UserPresenceEntityFields
import org.matrix.android.sdk.internal.util.database.RealmMigrator

/**
 * This migration make next fields required:
 * 'EventEntity.threadNotificationStateStr'
 * 'EventInsertEntity.insertTypeStr'
 * 'LocalRoomSummaryEntity.stateStr'
 * 'PushRulesEntity.kindStr'
 * 'PusherEntity.stateStr'
 * 'RoomEntity.membershipStr'
 * 'RoomEntity.membersLoadStatusStr'
 * 'RoomMemberSummaryEntity.membershipStr'
 * 'RoomSummaryEntity.membershipStr'
 * 'RoomSummaryEntity.versioningStateStr'
 * 'UserPresenceEntity.presenceStr'
 */
internal class MigrateSessionTo056(realm: DynamicRealm) : RealmMigrator(realm, 56) {

    override fun doMigrate(realm: DynamicRealm) {
        with(realm.schema) {
            get("EventEntity")?.setRequired(EventEntityFields.THREAD_NOTIFICATION_STATE_STR, true)
            get("EventInsertEntity")?.setRequired(EventInsertEntityFields.INSERT_TYPE_STR, true)
            get("LocalRoomSummaryEntity")?.setRequired(LocalRoomSummaryEntityFields.STATE_STR, true)
            get("PushRulesEntity")?.setRequired(PushRulesEntityFields.KIND_STR, true)
            get("PusherEntity")?.setRequired(PusherEntityFields.STATE_STR, true)
            get("RoomEntity")?.apply {
                setRequired(RoomEntityFields.MEMBERSHIP_STR, true)
                setRequired(RoomEntityFields.MEMBERS_LOAD_STATUS_STR, true)
            }
            get("RoomMemberSummaryEntity")?.setRequired(RoomMemberSummaryEntityFields.MEMBERSHIP_STR, true)
            get("RoomSummaryEntity")?.apply {
                setRequired(RoomSummaryEntityFields.MEMBERSHIP_STR, true)
                setRequired(RoomSummaryEntityFields.VERSIONING_STATE_STR, true)
            }
            get("UserPresenceEntity")?.setRequired(UserPresenceEntityFields.PRESENCE_STR, true)
        }
    }
}
