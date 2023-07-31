package com.arvifox.remysaw

import java.math.BigInteger
import jp.co.soramitsu.shared_utils.encrypt.EncryptionType
import jp.co.soramitsu.shared_utils.extensions.fromHex
import jp.co.soramitsu.shared_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.shared_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.shared_utils.runtime.metadata.module
import jp.co.soramitsu.shared_utils.runtime.metadata.storage
import jp.co.soramitsu.shared_utils.runtime.metadata.storageKey
import jp.co.soramitsu.shared_utils.ss58.SS58Encoder.toAccountId

object SubstrateOptionsProvider {
    const val hex = "0x"
//    const val testAddress = "cnVkoGs3rEMqLqY27c2nfVXJRGdzNJk2ns78DcqtppaSRe8qm"
    const val testAddress = "cnUVLAjzRsrXrzEiqjxMpBwvb6YgdBy8DKibonvZgtcQY5ZKe"
    const val mortalEraLength = 64
    const val runtimeVersion = 58
    const val precision = 18
    const val syntheticTokenRegex = "0[xX]03[0-9a-fA-F]+"
    val encryptionType = EncryptionType.SR25519
    val existentialDeposit: BigInteger = BigInteger.ZERO
    const val feeAssetId = "0x0200000000000000000000000000000000000000000000000000000000000000"
    const val pswapAssetId = "0x0200050000000000000000000000000000000000000000000000000000000000"
    const val xstTokenId = "0x0200090000000000000000000000000000000000000000000000000000000000"
    const val xstusdTokenId = "0x0200080000000000000000000000000000000000000000000000000000000000"
    const val ethTokenId = "0x0200070000000000000000000000000000000000000000000000000000000000"
}

fun String.mapAssetId() = this.fromHex().mapAssetId()
fun ByteArray.mapAssetId() = this.toList().map { it.toInt().toBigInteger() }

fun RuntimeSnapshot.accountPoolsKey(address: String): String =
    this.metadata.module(Pallete.POOL_XYK.palletName)
        .storage(Storage.ACCOUNT_POOLS.storageName)
        .storageKey(this, address.toAccountId())

fun RuntimeSnapshot.poolTBCReserves(tokenId: ByteArray): String =
    this.metadata.module(Pallete.POOL_TBC.palletName)
        .storage(Storage.RESERVES_COLLATERAL.storageName)
        .storageKey(
            this,
            Struct.Instance(
                mapOf(
                    "code" to tokenId.mapAssetId()
                )
            )
        )

fun RuntimeSnapshot.reservesKey(baseTokenId: String, tokenId: ByteArray): String =
    this.metadata.module(Pallete.POOL_XYK.palletName)
        .storage(Storage.RESERVES.storageName)
        .storageKey(
            this,
            Struct.Instance(
                mapOf(
                    "code" to baseTokenId.mapAssetId()
                )
            ),
            Struct.Instance(
                mapOf(
                    "code" to tokenId.mapAssetId()
                )
            )
        )

fun RuntimeSnapshot.reservesKeyToken(baseTokenId: String): String =
    this.metadata.module(Pallete.POOL_XYK.palletName)
        .storage(Storage.RESERVES.storageName)
        .storageKey(
            this,
            Struct.Instance(
                mapOf(
                    "code" to baseTokenId.mapAssetId()
                )
            ),
        )

enum class Pallete(val palletName: String) {
    ASSETS("Assets"),
    IROHA_MIGRATION("IrohaMigration"),
    SYSTEM("System"),
    LIQUIDITY_PROXY("LiquidityProxy"),
    POOL_XYK("PoolXYK"),
    POOL_TBC("MulticollateralBondingCurvePool"),
    STAKING("Staking"),
    TRADING_PAIR("TradingPair"),
    UTILITY("Utility"),
    FAUCET("Faucet"),
    Referrals("Referrals"),
    DEX_MANAGER("DEXManager"),
    XSTPool("XSTPool"),
    TOKENS("Tokens"),
    DEMETER_FARMING("DemeterFarmingPlatform"),
    ETH_BRIDGE("EthBridge"),
}

enum class Storage(val storageName: String) {
    ASSET_INFOS("AssetInfos"),
    ACCOUNT("Account"),
    ACCOUNTS("Accounts"),
    RESERVES("Reserves"),
    RESERVES_COLLATERAL("CollateralReserves"),
    LEDGER("Ledger"),
    ACTIVE_ERA("ActiveEra"),
    BONDED("Bonded"),
    UPGRADED_TO_DUAL_REF_COUNT("UpgradedToDualRefCount"),
    ACCOUNT_POOLS("AccountPools"),
    PROPERTIES("Properties"),
    TOTAL_ISSUANCES("TotalIssuances"),
    POOL_PROVIDERS("PoolProviders"),
    REFERRER_BALANCE("ReferrerBalances"),
    REFERRERS("Referrers"),
    REFERRALS("Referrals"),
    DEX_INFOS("DEXInfos"),
    BASE_FEE("BaseFee"),
    USER_INFOS("UserInfos"),
}

enum class Method(val methodName: String) {
    TRANSFER("transfer"),
    MIGRATE("migrate"),
    SWAP("swap"),
    REGISTER("register"),
    INITIALIZE_POOL("initialize_pool"),
    DEPOSIT_LIQUIDITY("deposit_liquidity"),
    WITHDRAW_LIQUIDITY("withdraw_liquidity"),
    BATCH_ALL("batch_all"),
    BATCH("batch"),
    SET_REFERRER("set_referrer"),
    UNRESERVE("unreserve"),
    RESERVE("reserve"),
    TRANSFER_TO_SIDECHAIN("transfer_to_sidechain"),
}

enum class Events(val eventName: String) {
    EXTRINSIC_SUCCESS("ExtrinsicSuccess"),
    EXTRINSIC_FAILED("ExtrinsicFailed"),
}

enum class Constants(val constantName: String) {
    SS58Prefix("SS58Prefix")
}
