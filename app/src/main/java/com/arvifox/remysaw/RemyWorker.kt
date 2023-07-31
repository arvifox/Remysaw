package com.arvifox.remysaw

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.arvifox.remysaw.ext.greaterThan
import com.arvifox.remysaw.ext.lessThan
import com.arvifox.remysaw.ext.mapBalance
import com.arvifox.remysaw.ext.safeCast
import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocketFactory
import jp.co.soramitsu.shared_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.shared_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.shared_utils.runtime.metadata.module
import jp.co.soramitsu.shared_utils.runtime.metadata.storage
import jp.co.soramitsu.shared_utils.runtime.metadata.storageKey
import jp.co.soramitsu.shared_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.shared_utils.wsrpc.SocketService
import jp.co.soramitsu.shared_utils.wsrpc.logging.Logger
import jp.co.soramitsu.shared_utils.wsrpc.recovery.ConstantReconnectStrategy
import jp.co.soramitsu.shared_utils.wsrpc.recovery.Reconnector
import jp.co.soramitsu.shared_utils.wsrpc.request.RequestExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformWhile
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class RemyWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val tag = "RemyWorker"
        const val NOTIFICATION_ID = 42

        fun start(c: Context) {
            val r = OneTimeWorkRequestBuilder<RemyWorker>()
                .addTag(tag)
                .build()
            Timber.e("foxx enquueue")
            WorkManager.getInstance(c).enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, r)
        }

        fun getInfo(c: Context) =
            WorkManager.getInstance(c).getWorkInfosForUniqueWorkLiveData(tag)

        fun stop(c: Context) =
            WorkManager.getInstance(c).cancelUniqueWork(tag)
    }

    private val notificationManager = NotificationManagerCompat.from(appContext)
    private lateinit var rm: RuntimeManager
    private lateinit var wsm: WsConnectionManager
    private val gson = Gson()
    private lateinit var subc: SubstrateCalls
    private lateinit var ss: SocketService

    override suspend fun doWork(): Result {
        val no = createNoti()
        setForeground(ForegroundInfo(NOTIFICATION_ID, no))
        Timber.e("foxx dowork")
        RuntimeManager(gson, appContext).also {
            it.initManager()
            rm = it
        }
        ss = SocketService(
            gson,
            object : Logger {
                override fun log(message: String?) {
                    Timber.e("remy wslog $message")
                }

                override fun log(throwable: Throwable?) {
                    Timber.e("remy wsthr ${throwable?.message}")
                }
            },
            WebSocketFactory(),
            Reconnector(ConstantReconnectStrategy(10000L)),
            RequestExecutor(),
        )
        wsm = WsConnectionManager(ss)
        wsm.setAddress("wss://ws.framenode-7.s4.stg1.sora2.soramitsu.co.jp")
//        wsm?.setAddress("wss://mof2.sora.org")
        wsm.start()
        subc = SubstrateCalls(ss, rm)
        val startBalance = subc.getBalance()
        val needBalance = startBalance + BigDecimal.valueOf(500)
        delay(1000)

        val storage = rm.getRuntimeSnapshot().metadata.module(Pallete.SYSTEM.palletName)
            .storage(Storage.ACCOUNT.storageName)
        val key = storage
            .storageKey(rm.getRuntimeSnapshot(), SubstrateOptionsProvider.testAddress.toAccountId())
        val value = subc.observeStorage(key)
            .catch {
                Timber.e(it, "balance subscription error")
            }
            .transformWhile {
                val type = storage.type.value!!
                val raw = type.fromHex(rm.getRuntimeSnapshot(), it)
                val data = raw.safeCast<Struct.Instance>()?.get<Struct.Instance>("data")
                val free = data?.get<BigInteger>("free")!!
                val mapped = mapBalance(free, SubstrateOptionsProvider.precision)
                Timber.e("mapbalance $mapped $needBalance")
                emit(if (mapped.lessThan(needBalance)) null else mapped)
                mapped.lessThan(needBalance)
            }.filterNotNull().first()


        Timber.e("foxx dowork done $value")
        wsm.stop()
        return Result.success()
    }

    private fun createNoti(): Notification {
        RemyNotification.checkNotificationChannel(notificationManager)
        val notification = RemyNotification.getBuilder(appContext)
            .setContentTitle("Remy is looking for ...")
            .build()
        return notification
    }
}
