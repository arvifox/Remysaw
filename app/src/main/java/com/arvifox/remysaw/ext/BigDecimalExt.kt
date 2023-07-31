package com.arvifox.remysaw.ext

import com.arvifox.remysaw.SubstrateOptionsProvider
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

val Big100 = BigDecimal.valueOf(100)

fun compareNullDesc(o1: BigDecimal?, o2: BigDecimal?): Int =
    when {
        o1 == null && o2 == null -> 0
        o1 != null && o2 != null -> o2.compareTo(o1)
        o1 == null -> 1
        else -> -1
    }

fun BigDecimal.isZero(): Boolean = this.compareTo(BigDecimal.ZERO) == 0

fun BigDecimal?.multiplyNullable(decimal: BigDecimal?): BigDecimal? =
    if (this != null && decimal != null) this.multiply(decimal) else null

fun BigDecimal.equalTo(a: BigDecimal) = this.compareTo(a) == 0

fun BigDecimal.greaterThan(a: BigDecimal) = this.compareTo(a) == 1
fun BigDecimal.lessThan(a: BigDecimal) = this.compareTo(a) == -1

fun BigDecimal?.orZero(): BigDecimal = this ?: BigDecimal.ZERO

fun BigDecimal.nullZero(): BigDecimal? = if (this.isZero()) null else this

fun BigDecimal.divideBy(
    divisor: BigDecimal,
    scale: Int? = null
): BigDecimal {
    return if (scale == null) {
        val maxScale = max(this.scale(), divisor.scale())

        if (maxScale != 0) {
            this.divide(divisor, maxScale, RoundingMode.HALF_EVEN)
        } else {
            this.divide(divisor, SubstrateOptionsProvider.precision, RoundingMode.HALF_EVEN)
        }
    } else {
        this.divide(divisor, scale, RoundingMode.HALF_EVEN)
    }
}

fun BigDecimal.safeDivide(
    divisor: BigDecimal,
    scale: Int? = null
): BigDecimal {
    return if (divisor.isZero()) {
        BigDecimal.ZERO
    } else {
        divideBy(divisor, scale)
    }
}
