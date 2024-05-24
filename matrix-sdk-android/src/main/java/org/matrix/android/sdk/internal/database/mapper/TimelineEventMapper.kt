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

package org.matrix.android.sdk.internal.database.mapper

import org.matrix.android.sdk.api.session.events.model.Event
import org.matrix.android.sdk.api.session.room.sender.SenderInfo
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.internal.database.RealmSessionProvider
import org.matrix.android.sdk.internal.database.model.TimelineEventEntity
import org.matrix.android.sdk.internal.database.model.UserEntity
import org.matrix.android.sdk.internal.database.query.where
import javax.inject.Inject

internal class TimelineEventMapper @Inject constructor(
        private val readReceiptsSummaryMapper: ReadReceiptsSummaryMapper,
        private val realmSessionProvider: RealmSessionProvider
) {

    fun map(timelineEventEntity: TimelineEventEntity, buildReadReceipts: Boolean = true): TimelineEvent {
        val readReceipts = if (buildReadReceipts) {
            timelineEventEntity.readReceipts
                    ?.let {
                        readReceiptsSummaryMapper.map(it)
                    }
        } else {
            null
        }
        return TimelineEvent(
                root = timelineEventEntity.root?.asDomain()
                        ?: Event("", timelineEventEntity.eventId),
                eventId = timelineEventEntity.eventId,
                annotations = timelineEventEntity.annotations?.asDomain(),
                localId = timelineEventEntity.localId,
                displayIndex = timelineEventEntity.displayIndex,
                senderInfo = getSenderInfoFromPresenceUpdate(timelineEventEntity),
                ownedByThreadChunk = timelineEventEntity.ownedByThreadChunk,
                readReceipts = readReceipts
                        ?.distinctBy {
                            it.roomMember
                        }?.sortedByDescending {
                            it.originServerTs
                        }.orEmpty()
        )
    }

    //Added for Circles
    private fun getSenderInfoFromPresenceUpdate(timelineEventEntity: TimelineEventEntity): SenderInfo {
        val senderId = timelineEventEntity.root?.sender ?: ""
        val user = realmSessionProvider.withRealm {
            val userEntity = UserEntity.where(it, senderId).findFirst()
            userEntity?.asDomain()
        }
        return SenderInfo(
                userId = senderId,
                displayName = user?.displayName ?: timelineEventEntity.senderName,
                isUniqueDisplayName = timelineEventEntity.isUniqueDisplayName,
                avatarUrl = user?.avatarUrl ?: timelineEventEntity.senderAvatar
        )
    }
}
