package org.matrix.android.sdk.internal.crypto.dehydrated

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.matrix.android.sdk.api.MatrixCoroutineDispatchers
import org.matrix.android.sdk.api.crypto.MEGOLM_DEFAULT_ROTATION_PERIOD_MS
import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.api.session.securestorage.KeyInfoResult
import org.matrix.android.sdk.api.session.securestorage.KeyRef
import org.matrix.android.sdk.api.session.securestorage.RawBytesKeySpec
import org.matrix.android.sdk.api.session.securestorage.SharedSecretStorageService
import org.matrix.android.sdk.api.util.fromBase64
import org.matrix.android.sdk.api.util.toBase64NoPadding
import org.matrix.android.sdk.internal.crypto.OlmMachine
import org.matrix.android.sdk.internal.crypto.model.rest.DehydratedDeviceEventsResponse
import org.matrix.android.sdk.internal.crypto.model.rest.GetDehydratedDeviceResponse
import org.matrix.android.sdk.internal.crypto.tasks.CreateDehydratedDeviceTask
import org.matrix.android.sdk.internal.crypto.tasks.GetDehydratedDeviceEventsTask
import org.matrix.android.sdk.internal.crypto.tasks.GetDehydratedDeviceTask
import org.matrix.android.sdk.internal.di.DeviceId
import timber.log.Timber
import java.security.SecureRandom
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
        private val ssssService: SharedSecretStorageService
) {
    private var isDehydrationRunning = false
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    suspend fun handleDehydratedDevice() {
        try {
            if (isDehydrationRunning || isDeviceDehydrationRequired().not()) return
            isDehydrationRunning = true
            Timber.tag(LOG_TAG).d("start")
            val (defaultKeyId, bsSpekeKey) = getDefaultSSKey()
            val ssPickleKey = getPickleKey(defaultKeyId, bsSpekeKey)
            val existingDehydratedDevice = getDehydratedDevice()
            Timber.tag(LOG_TAG).d("existing device $existingDehydratedDevice")
            existingDehydratedDevice?.deviceId?.let { deviceId ->
                rehydrateDevice(ssPickleKey, deviceId, existingDehydratedDevice.deviceData)
            }
            val newPickleKey = generateAndStoreDehydratedDeviceKey(defaultKeyId, bsSpekeKey)
            createDehydratedDevice(newPickleKey)
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

    private suspend fun getPickleKey(defaultKeyId: String, bsSpekeKey: ByteArray): ByteArray {
        return getDehydratedDeviceKey(defaultKeyId, bsSpekeKey)
                ?: generateAndStoreDehydratedDeviceKey(defaultKeyId, bsSpekeKey)
    }

    private fun getDefaultSSKey(): Pair<String, ByteArray> {
        val defaultKeyId = (ssssService.getDefaultKey() as? KeyInfoResult.Success)?.keyInfo?.id
                ?: throw IllegalStateException("Default key not found")
        val bsSpekeKey = ssssService.getBsSpekePrivateKey(defaultKeyId)
                ?: throw IllegalStateException("BsSpeke key not found")
        return defaultKeyId to bsSpekeKey
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

    private suspend fun getDehydratedDeviceKey(defaultKeyId: String, bsSpekeKey: ByteArray): ByteArray? = tryOrNull {
        ssssService.getSecret(
                name = ORG_FUTO_SSSS_KEY_DEHYDRATED_DEVICE,
                keyId = defaultKeyId,
                secretKey = RawBytesKeySpec(bsSpekeKey)
        ).fromBase64()
    }

    private suspend fun generateAndStoreDehydratedDeviceKey(defaultKeyId: String, bsSpekeKey: ByteArray): ByteArray {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        ssssService.storeSecret(
                name = ORG_FUTO_SSSS_KEY_DEHYDRATED_DEVICE,
                secretBase64 = bytes.toBase64NoPadding(),
                keys = listOf(KeyRef(defaultKeyId, RawBytesKeySpec(bsSpekeKey)))
        )
        return bytes
    }

    companion object {
        private const val ORG_FUTO_SSSS_KEY_DEHYDRATED_DEVICE = "org.futo.ssss.key.dehydrated_device"
        private const val LOG_TAG = "DehydratedDevice"
        private const val PREF_NAME = "org.futo.circles.dehydrated"
        private const val DEHYDRATION_TIME_PREFIX = "last_device_dehydration_"
    }
}