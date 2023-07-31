package com.arvifox.remysaw

import androidx.annotation.Keep
import com.arvifox.remysaw.ext.extrinsicHash
import com.arvifox.remysaw.ext.mapBalance
import com.arvifox.remysaw.ext.removeHexPrefix
import com.arvifox.remysaw.ext.safeCast
import jp.co.soramitsu.core.runtime.models.requests.NextAccountIndexRequest
import jp.co.soramitsu.shared_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.shared_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.shared_utils.runtime.definitions.types.fromHex
import jp.co.soramitsu.shared_utils.runtime.definitions.types.generics.GenericEvent
import jp.co.soramitsu.shared_utils.runtime.metadata.module
import jp.co.soramitsu.shared_utils.runtime.metadata.storage
import jp.co.soramitsu.shared_utils.runtime.metadata.storageKey
import jp.co.soramitsu.shared_utils.wsrpc.SocketService
import jp.co.soramitsu.shared_utils.wsrpc.executeAsync
import jp.co.soramitsu.shared_utils.wsrpc.mappers.nonNull
import jp.co.soramitsu.shared_utils.wsrpc.mappers.pojo
import jp.co.soramitsu.shared_utils.wsrpc.mappers.pojoList
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.RuntimeRequest
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.author.SubmitAndWatchExtrinsicRequest
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.author.SubmitExtrinsicRequest
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.chain.RuntimeVersion
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.chain.RuntimeVersionRequest
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.storage.GetStorageRequest
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.storage.SubscribeStorageRequest
import jp.co.soramitsu.shared_utils.wsrpc.request.runtime.storage.storageChange
import jp.co.soramitsu.shared_utils.wsrpc.subscriptionFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class StateKeys(params: List<Any>) : RuntimeRequest("state_getKeys", params)
class StateKeysPaged(params: List<Any>) : RuntimeRequest("state_getKeysPaged", params)
class StateQueryStorageAt(params: List<Any>) : RuntimeRequest("state_queryStorageAt", params)
class BlockHashRequest(number: Int) : RuntimeRequest("chain_getBlockHash", listOf(number)) {
    constructor(n: String) : this(n.removeHexPrefix().toInt(16))
}
class FinalizedHeadRequest : RuntimeRequest("chain_getFinalizedHead", listOf())
@Keep
class ChainHeaderResponse(val parentHash: String, val number: String, val stateRoot: String, val extrinsicsRoot: String)
class ChainHeaderRequest(hash: String) : RuntimeRequest("chain_getHeader", listOf(hash))
class ChainLastHeaderRequest : RuntimeRequest("chain_getHeader", listOf())
class BlockRequest(blockHash: String) : RuntimeRequest("chain_getBlock", listOf(blockHash))
@Keep
data class BlockResponse(val justification: Any?, val block: BlockEntry)
@Keep
data class BlockEntry(val header: Any?, val extrinsics: List<String>)
data class BlockEvent(val module: Any, val event: Any, val number: Long?)

class ChainBalance(val balance: String)

sealed class ExtrinsicStatusResponse(val subscription: String) {
    class ExtrinsicStatusPending(subscription: String) : ExtrinsicStatusResponse(subscription)
    class ExtrinsicStatusFinalized(subscription: String, val inBlock: String) : ExtrinsicStatusResponse(subscription)
    class ExtrinsicStatusFinalityTimeout(subscription: String) : ExtrinsicStatusResponse(subscription)
}

class SubstrateCalls constructor(
    private val socketService: SocketService,
    private val runtimeManager: RuntimeManager,
) {

    companion object {
        const val FINALIZED = "finalized"
        private const val FINALITY_TIMEOUT = "finalityTimeout"
        const val IN_BLOCK = "inBlock"
        const val DEFAULT_ASSETS_PAGE_SIZE = 100
    }

    suspend fun getStorageHex(storageKey: String): String? =
        socketService.executeAsync(
            request = GetStorageRequest(listOf(storageKey)),
            mapper = pojo<String>(),
        ).result

    suspend fun getBalance(): BigDecimal {
        val b = socketService.executeAsync(
            request = RuntimeRequest(
                "assets_usableBalance",
                listOf(SubstrateOptionsProvider.testAddress, "0x0200000000000000000000000000000000000000000000000000000000000000")),
            mapper = pojo<ChainBalance>().nonNull(),
        )
        return mapBalance(BigInteger(b.balance), SubstrateOptionsProvider.precision)
    }

    suspend fun getStateKeys(partialKey: String): List<String> =
        socketService.executeAsync(
            request = StateKeys(listOf(partialKey)),
            mapper = pojoList<String>(),
        ).result ?: emptyList()

    suspend fun submitExtrinsic(
        extrinsic: String,
    ): String {
        return socketService.executeAsync(
            request = SubmitExtrinsicRequest(extrinsic),
            mapper = pojo<String>().nonNull(),
        )
    }

    fun submitAndWatchExtrinsic(
        extrinsic: String,
        finalizedKey: String = IN_BLOCK,
    ): Flow<Pair<String, ExtrinsicStatusResponse>> {
        val hash = extrinsic.extrinsicHash()
        return socketService.subscriptionFlow(
            request = SubmitAndWatchExtrinsicRequest(extrinsic),
            unsubscribeMethod = "author_unwatchExtrinsic",
        ).map {
            val subscriptionId = it.subscriptionId
            val result = it.params.result
            val mapped = result.safeCast<Map<String, *>>()
            val statusResponse: ExtrinsicStatusResponse = when {
                mapped?.containsKey(finalizedKey) ?: false ->
                    ExtrinsicStatusResponse.ExtrinsicStatusFinalized(
                        subscriptionId,
                        mapped?.getValue(finalizedKey) as String
                    )

                mapped?.containsKey(FINALITY_TIMEOUT) ?: false ->
                    ExtrinsicStatusResponse.ExtrinsicStatusFinalityTimeout(subscriptionId)

                else -> ExtrinsicStatusResponse.ExtrinsicStatusPending(subscriptionId)
            }
            hash to statusResponse
        }
    }

    suspend fun getNonce(from: String): BigInteger {
        return socketService.executeAsync(
            request = NextAccountIndexRequest(from),
            mapper = pojo<Double>().nonNull()
        )
            .toInt().toBigInteger()
    }

    suspend fun getBlockHash(number: Int = 0): String {
        return socketService.executeAsync(
            request = BlockHashRequest(number),
            mapper = pojo<String>().nonNull(),
        )
    }

    suspend fun getRuntimeVersion(): RuntimeVersion {
        return socketService.executeAsync(
            request = RuntimeVersionRequest(),
            mapper = pojo<RuntimeVersion>().nonNull(),
        )
    }

    suspend fun getFinalizedHead(): String {
        return socketService.executeAsync(
            request = FinalizedHeadRequest(),
            mapper = pojo<String>().nonNull()
        )
    }

    suspend fun getChainHeader(hash: String): ChainHeaderResponse {
        return socketService.executeAsync(
            request = ChainHeaderRequest(hash),
            mapper = pojo<ChainHeaderResponse>().nonNull(),
        )
    }

    suspend fun getChainLastHeader(): ChainHeaderResponse {
        return socketService.executeAsync(
            request = ChainLastHeaderRequest(),
            mapper = pojo<ChainHeaderResponse>().nonNull(),
        )
    }

    suspend fun checkEvents(
        blockHash: String
    ): List<BlockEvent> {
        val storageKey =
            runtimeManager.getRuntimeSnapshot().metadata.module("System").storage("Events")
                .storageKey()
        return runCatching {
            socketService.executeAsync(
                request = GetStorageRequest(listOf(storageKey, blockHash)),
                mapper = pojo<String>().nonNull(),
            )
                .let { storage ->
                    val eventType =
                        runtimeManager.getRuntimeSnapshot().metadata.module("System")
                            .storage("Events").type.value!!
                    val eventsRaw = eventType.fromHex(runtimeManager.getRuntimeSnapshot(), storage)
                    if (eventsRaw is List<*>) {
                        val eventRecordList =
                            eventsRaw.filterIsInstance<Struct.Instance>().mapNotNull {
                                val phase = it.get<DictEnum.Entry<*>>("phase")
                                val phaseValue = when (phase?.name) {
                                    "ApplyExtrinsic" -> phase.value as BigInteger
                                    "Finalization" -> null
                                    "Initialization" -> null
                                    else -> null
                                }
                                if (phaseValue != null) {
                                    val eventInstance = it.get<GenericEvent.Instance>("event")
                                    if (eventInstance != null) {
                                        BlockEvent(
                                            eventInstance.module.index.toInt(),
                                            eventInstance.event.index.second,
                                            phaseValue.toLong(),
                                        )
                                    } else {
                                        val enumInstance = it.get<DictEnum.Entry<*>>("event")
                                        if (enumInstance != null) {
                                            BlockEvent(
                                                enumInstance.name,
                                                enumInstance.value?.safeCast<DictEnum.Entry<*>>()?.name.orEmpty(),
                                                phaseValue.toLong(),
                                            )
                                        } else {
                                            null
                                        }
                                    }
                                } else null
                            }
                        eventRecordList
                    } else emptyList()
                }
        }.getOrElse {
            Timber.e(it, it.localizedMessage)
            emptyList()
        }
    }

    suspend fun getBlock(blockHash: String): BlockResponse {
        return socketService.executeAsync(
            request = BlockRequest(blockHash),
            mapper = pojo<BlockResponse>().nonNull(),
        )
    }

    fun observeStorage(key: String): Flow<String> {
        return socketService.subscriptionFlow(
            SubscribeStorageRequest(key),
            "state_unsubscribeStorage"
        )
            .map {
                it.storageChange().getSingleChange().orEmpty()
            }
    }
}
