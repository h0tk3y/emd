package com.github.h0tk3y.emd

import java.math.BigDecimal

class BigDecimalEmd<T>(distance: ((T, T) -> BigDecimal)? = null)
: Emd<T, BigDecimal>(distance) {
    override val zero: BigDecimal = BigDecimal.ZERO
    override fun BigDecimal.plus(other: BigDecimal) = this + other
    override fun BigDecimal.minus(other: BigDecimal) = this - other
    override fun BigDecimal.times(other: BigDecimal) = this * other
}

/**
 * [Emd] implementation suitable for small [Int]s that won't cause numerical overflow.
 */
class IntEmd<T>(distance: ((T, T) -> Int)? = null)
: Emd<T, Int>(distance) {
    override val zero: Int = 0
    override fun Int.plus(other: Int) = this + other
    override fun Int.minus(other: Int) = this - other
    override fun Int.times(other: Int) = this * other
}