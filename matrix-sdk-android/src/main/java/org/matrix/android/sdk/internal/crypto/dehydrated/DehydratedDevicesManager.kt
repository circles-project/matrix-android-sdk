package org.matrix.android.sdk.internal.crypto.dehydrated

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.matrix.android.sdk.api.MatrixCoroutineDispatchers
import org.matrix.android.sdk.api.crypto.MEGOLM_DEFAULT_ROTATION_PERIOD_MS
import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.api.session.crypto.keysbackup.KeysBackupService
import org.matrix.android.sdk.api.session.crypto.keysbackup.extractCurveKeyFromRecoveryKey
import org.matrix.android.sdk.internal.crypto.OlmMachine
import org.matrix.android.sdk.internal.crypto.model.rest.DehydratedDeviceEventsResponse
import org.matrix.android.sdk.internal.crypto.model.rest.GetDehydratedDeviceResponse
import org.matrix.android.sdk.internal.crypto.tasks.CreateDehydratedDeviceTask
import org.matrix.android.sdk.internal.crypto.tasks.GetDehydratedDeviceEventsTask
import org.matrix.android.sdk.internal.crypto.tasks.GetDehydratedDeviceTask
import org.matrix.android.sdk.internal.di.DeviceId
import timber.log.Timber
import javax.inject.Inject

//Added for Circles
internal class DehydratedDevicesManager @Inject constructor(
        context: Context,
        @DeviceId private val myDeviceId: String,
        private val olmMachine: OlmMachine,
        private val coroutineDispatchers: MatrixCoroutineDispatchers,
        private val createDehydratedDeviceTask: CreateDehydratedDeviceTask,
        private val getDehydratedDeviceTask: GetDehydratedDeviceTask,
        private val getDehydratedDeviceEventsTask: GetDehydratedDeviceEventsTask,
        private val keysBackupService: KeysBackupService
) {
    private var isDehydrationRunning = false
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    suspend fun handleDehydratedDevice() {
        try {
            if (isDehydrationRunning || isDeviceDehydrationRequired().not()) return
            isDehydrationRunning = true
            Timber.tag(LOG_TAG).d("start")
            val ssKey = getKey()
            val existingDehydratedDevice = getDehydratedDevice()
            Timber.tag(LOG_TAG).d("existing device $existingDehydratedDevice")
            existingDehydratedDevice?.deviceId?.let { deviceId ->
                rehydrateDevice(ssKey, deviceId, existingDehydratedDevice.deviceData)
            }
            createDehydratedDevice(ssKey)
            saveLastDehydrationTime()
            Timber.tag(LOG_TAG).d("dehydration time ${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Timber.tag(LOG_TAG).d("$e")
        }
        isDehydrationRunning = false
    }

    private fun isDeviceDehydrationRequired(): Boolean {
        val lastDehydrationTime = sharedPreferences.getLong(getLastDehydrationTimeKey(), 0L)
                .takeIf { it != 0L } ?: return true
        return (System.currentTimeMillis() - lastDehydrationTime) > MEGOLM_DEFAULT_ROTATION_PERIOD_MS
    }

    private fun saveLastDehydrationTime() {
        sharedPreferences.edit { putLong(getLastDehydrationTimeKey(), System.currentTimeMillis()) }
    }

    private fun getLastDehydrationTimeKey(): String = DEHYDRATION_TIME_PREFIX + myDeviceId

    private suspend fun getKey(): ByteArray {
        val recoveryKey = keysBackupService.getKeyBackupRecoveryKeyInfo()?.recoveryKey?.toBase58()
                ?: throw Exception("Recovery Key not found")
        val secret = extractCurveKeyFromRecoveryKey(recoveryKey)
                ?: throw Exception("Can not get secret from recovery key")
        return secret
    }

    private suspend fun rehydrateDevice(pickleKey: ByteArray, deviceId: String, deviceData: Map<String, String>) {
        val deviceDataJson = JSONObject(deviceData).toString()
        val rehydrator = withContext(coroutineDispatchers.crypto) {
            olmMachine.inner().dehydratedDevices().rehydrate(pickleKey, deviceId, deviceDataJson)
        }
        Timber.tag(LOG_TAG).d("created rehydrator")
        var nextBatchToken = ""

        withContext(coroutineDispatchers.io) {
            while (true) {
                val eventsResponse = getDehydratedDeviceEvents(deviceId, nextBatchToken)
                Timber.tag(LOG_TAG).d("events $eventsResponse")
                if (eventsResponse.events.isEmpty()) break

                nextBatchToken = eventsResponse.nextBatch ?: ""
                val eventsJson = JSONArray(eventsResponse.events).toString()
                rehydrator.receiveEvents(eventsJson)
            }
        }
    }

    private suspend fun getDehydratedDevice(): GetDehydratedDeviceResponse? {
        return tryOrNull {
            withContext(coroutineDispatchers.io) {
                getDehydratedDeviceTask.execute(Unit)
            }
        }
    }

    private suspend fun getDehydratedDeviceEvents(deviceId: String, nextBatch: String): DehydratedDeviceEventsResponse {
        return withContext(coroutineDispatchers.io) {
            getDehydratedDeviceEventsTask.execute(
                    GetDehydratedDeviceEventsTask.Params(deviceId, nextBatch)
            )
        }
    }

    private suspend fun createDehydratedDevice(pickleKey: ByteArray): String? {
        val request = withContext(coroutineDispatchers.crypto) {
            val innerOlm = olmMachine.inner()
            val dehydratedDevice = innerOlm.dehydratedDevices().create()
            dehydratedDevice.keysForUpload("$myDeviceId (dehydrated)", pickleKey)
        }
        val dehydratedDeviceIdResponse = withContext(coroutineDispatchers.io) {
            createDehydratedDeviceTask.execute(request)
        }
        Timber.tag(LOG_TAG).d("device created $dehydratedDeviceIdResponse")
        return dehydratedDeviceIdResponse.deviceId
    }

    companion object {
        private const val LOG_TAG = "DehydratedDevice"
        private const val PREF_NAME = "org.futo.circles.dehydrated"
        private const val DEHYDRATION_TIME_PREFIX = "last_device_dehydration_"
    }
}