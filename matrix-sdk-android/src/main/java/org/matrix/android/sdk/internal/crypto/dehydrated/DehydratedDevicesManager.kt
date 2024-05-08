package org.matrix.android.sdk.internal.crypto.dehydrated

import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.matrix.android.sdk.api.MatrixCoroutineDispatchers
import org.matrix.android.sdk.api.session.Session
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
        private val session: Session,
        @DeviceId private val myDeviceId: String,
        private val olmMachine: OlmMachine,
        private val coroutineDispatchers: MatrixCoroutineDispatchers,
        private val createDehydratedDeviceTask: CreateDehydratedDeviceTask,
        private val getDehydratedDeviceTask: GetDehydratedDeviceTask,
        private val getDehydratedDeviceEventsTask: GetDehydratedDeviceEventsTask
) {
    private var isDehydrationRunning = false
    private val tag = "DehydratedDevice"

    suspend fun handleDehydratedDevice() {
        if (isDehydrationRunning) return
        isDehydrationRunning = true
        try {
            Timber.tag(tag).d("start")
            val ssKey = getKey()
            val existingDehydratedDevice = getDehydratedDevice()
            Timber.tag(tag).d("existing device $existingDehydratedDevice")
            existingDehydratedDevice.deviceId?.let { deviceId ->
                rehydrateDevice(ssKey, deviceId, existingDehydratedDevice.deviceData)
            }
            createDehydratedDevice(ssKey)
        } catch (e: Exception) {
            Timber.tag(tag).d("$e")
        }
        isDehydrationRunning = false
    }

    private suspend fun getKey(): ByteArray {
        val recoveryKey = session.cryptoService().keysBackupService()
                .getKeyBackupRecoveryKeyInfo()?.recoveryKey?.toBase58()
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
        Timber.tag(tag).d("created rehydrator")
        var nextBatchToken = ""

        withContext(coroutineDispatchers.io) {
            while (true) {
                val eventsResponse = getDehydratedDeviceEvents(deviceId, nextBatchToken)
                Timber.tag(tag).d("events $eventsResponse")
                if (eventsResponse.events.isEmpty()) break

                nextBatchToken = eventsResponse.nextBatch ?: ""
                val eventsJson = JSONArray(eventsResponse.events).toString()
                rehydrator.receiveEvents(eventsJson)
            }
        }
    }

    private suspend fun getDehydratedDevice(): GetDehydratedDeviceResponse {
        return withContext(coroutineDispatchers.io) {
            getDehydratedDeviceTask.execute(Unit)
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
        Timber.tag(tag).d("device created $dehydratedDeviceIdResponse")
        return dehydratedDeviceIdResponse.deviceId
    }
}