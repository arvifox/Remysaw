package com.arvifox.remysaw

import android.content.Context
import com.google.gson.Gson
import jp.co.soramitsu.shared_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.shared_utils.runtime.definitions.TypeDefinitionParser
import jp.co.soramitsu.shared_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.shared_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.shared_utils.runtime.definitions.registry.TypePreset
import jp.co.soramitsu.shared_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.shared_utils.runtime.definitions.registry.v14Preset
import jp.co.soramitsu.shared_utils.runtime.definitions.types.fromByteArrayOrNull
import jp.co.soramitsu.shared_utils.runtime.definitions.v14.TypesParserV14
import jp.co.soramitsu.shared_utils.runtime.metadata.RuntimeMetadataReader
import jp.co.soramitsu.shared_utils.runtime.metadata.builder.VersionedRuntimeBuilder
import jp.co.soramitsu.shared_utils.runtime.metadata.module
import jp.co.soramitsu.shared_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14
import jp.co.soramitsu.shared_utils.ss58.SS58Encoder
import jp.co.soramitsu.shared_utils.ss58.SS58Encoder.toAccountId
import jp.co.soramitsu.shared_utils.ss58.SS58Encoder.toAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigInteger

private const val SORA2_TYPES_FILE = "types_scalecodec_mobile.json"
private const val RUNTIME_METADATA_FILE = "sora2_metadata"

class RuntimeManager constructor(
    private val gson: Gson,
    private val context: Context,
) {

    private val mutex = Mutex()
    private var runtimeSnapshot: RuntimeSnapshot? = null
    private var prefix: Short = 69

    suspend fun initManager() {
        getRuntimeSnapshot()
    }

    fun isAddressOk(address: String): Boolean =
        runCatching { address.toAccountId() }.getOrNull() != null &&
                SS58Encoder.extractAddressByte(address) == prefix

    fun toSoraAddress(byteArray: ByteArray): String = byteArray.toAddress(prefix)
    fun toSoraAddressOrNull(byteArray: ByteArray?): String? =
        runCatching { byteArray?.toAddress(prefix) }.getOrNull()

    suspend fun getRuntimeSnapshot(): RuntimeSnapshot {
        return runtimeSnapshot ?: mutex.withLock {
            runtimeSnapshot ?: createRuntimeSnapshot()
        }
    }

    private suspend fun createRuntimeSnapshot(): RuntimeSnapshot = withContext(Dispatchers.IO) {
        initFromCache()
    }

    private fun initFromCache() =
        buildRuntimeSnapshot(
            getContentFromCache(RUNTIME_METADATA_FILE),
        )

    private fun getContentFromCache(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun rawTypesToTree(raw: String) = gson.fromJson(raw, TypeDefinitionsTree::class.java)

    private fun buildRuntimeSnapshot(
        metadata: String,
    ): RuntimeSnapshot {
        val runtimeMetadataReader = RuntimeMetadataReader.read(metadata)
        val typeRegistry =
            buildTypeRegistry14(getContentFromCache(SORA2_TYPES_FILE), runtimeMetadataReader)
        val runtimeMetadata =
            VersionedRuntimeBuilder.buildMetadata(runtimeMetadataReader, typeRegistry)
        val snapshot = RuntimeSnapshot(typeRegistry, runtimeMetadata)
        runtimeSnapshot = snapshot
        val valueConstant =
            snapshot.metadata.module(Pallete.SYSTEM.palletName).constants[Constants.SS58Prefix.constantName]
        prefix = (
                valueConstant?.type?.fromByteArrayOrNull(
                    snapshot,
                    valueConstant.value
                ) as? BigInteger
                )?.toShort() ?: 69
        return snapshot
    }

    private fun buildTypeRegistry14(
        sora2Raw: String,
        runtimeMetadataReader: RuntimeMetadataReader,
    ): TypeRegistry {
        return buildTypeRegistryCommon(
            {
                TypesParserV14.parse(
                    runtimeMetadataReader.metadata[RuntimeMetadataSchemaV14.lookup],
                    v14Preset()
                ).typePreset
            },
            sora2Raw,
            { DynamicTypeResolver.defaultCompoundResolver() },
        )
    }

    private fun buildTypeRegistryCommon(
        defaultTypePresetBuilder: () -> TypePreset,
        sora2TypesRaw: String,
        dynamicTypeResolverBuilder: () -> DynamicTypeResolver,
    ): TypeRegistry {
        val sora2TypeDefinitionsTree = rawTypesToTree(sora2TypesRaw)
        val sora2ParseResult = TypeDefinitionParser.parseNetworkVersioning(
            tree = sora2TypeDefinitionsTree,
            typePreset = defaultTypePresetBuilder.invoke(),
            currentRuntimeVersion = SubstrateOptionsProvider.runtimeVersion,
            upto14 = false,
        )
        if (sora2ParseResult.unknownTypes.isNotEmpty()) {
            Timber.e("BuildRuntimeSnapshot. " + sora2ParseResult.unknownTypes.size + " unknown types are found")
        }
        return TypeRegistry(
            types = sora2ParseResult.typePreset,
            dynamicTypeResolver = dynamicTypeResolverBuilder.invoke()
        )
    }
}
