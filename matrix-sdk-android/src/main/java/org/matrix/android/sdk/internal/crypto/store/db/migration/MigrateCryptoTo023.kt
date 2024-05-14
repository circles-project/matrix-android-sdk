package org.matrix.android.sdk.internal.crypto.store.db.migration

import io.realm.DynamicRealm
import org.matrix.android.sdk.internal.crypto.store.db.model.OutgoingKeyRequestEntityFields
import org.matrix.android.sdk.internal.util.database.RealmMigrator

/**
 * This migration make OutgoingKeyRequestEntity.requestStateStr required. Seems to be due to the upgrade to Kotlin 1.8.21.
 */
internal class MigrateCryptoTo023(realm: DynamicRealm) : RealmMigrator(realm, 23) {

    override fun doMigrate(realm: DynamicRealm) {
        realm.schema.get("OutgoingKeyRequestEntity")
                ?.setRequired(OutgoingKeyRequestEntityFields.REQUEST_STATE_STR, true)
    }
}