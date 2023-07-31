package com.arvifox.remysaw.ext

fun Double.isNanZero(): Double =
    if (this.isNaN()) 0.0 else this
