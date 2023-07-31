package com.arvifox.remysaw.ext

import java.math.BigInteger

fun <K, V> Map<K, V>.inverseMap() = map { Pair(it.value, it.key) }.toMap()

inline fun <T> Iterable<T>.sumByBigInteger(selector: (T) -> BigInteger): BigInteger {
    var sum: BigInteger = BigInteger.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
