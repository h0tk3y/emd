package com.github.h0tk3y.emd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class BigDecimalEmdTests {
    val iterations = 1000
    val histogramSize = 10
    val d = BigDecimalEmd<Int> { x, y -> (BigDecimal(x) - BigDecimal(y)).abs() }

    @Test fun symmetry() {
        repeat(iterations) {
            val h1 = random.intsSequence().take(histogramSize).associateBy ({ it }, { BigDecimal(random.nextInt(1000)) })
            val h2 = random.intsSequence().take(histogramSize).associateBy ({ it }, { BigDecimal(random.nextInt(1000)) })

            val r1 = d.histogramDistance(h1, h2)
            val r2 = d.histogramDistance(h2, h1)

            assertEquals(r1, r2)
        }
    }

    @Test fun triangle() {
        fun mapWithSum(targetSum: BigDecimal) = random.intsSequence(100).distinct().take(histogramSize)
                .map { Pair(random.nextInt(100), BigDecimal(Math.abs(random.nextInt(100)))) }
                .toMap(HashMap())
                .apply {
                    val sum = values.fold(BigDecimal.ZERO) { acc, i -> acc + i }
                    compute(keys.first()) { k, v -> v + targetSum - sum }
                }

        repeat(iterations) {
            val targetSum = BigDecimal(random.nextInt(1000) + 1000)
            val h1 = mapWithSum(targetSum)
            val h2 = mapWithSum(targetSum)
            val h3 = mapWithSum(targetSum)

            val r12 = d.histogramDistance(h1, h2)
            val r13 = d.histogramDistance(h1, h3)
            val r23 = d.histogramDistance(h2, h3)

            assertTrue(r12 + r23 >= r13)
            assertTrue(r13 + r23 >= r12)
            assertTrue(r12 + r13 >= r23)
        }
    }
}