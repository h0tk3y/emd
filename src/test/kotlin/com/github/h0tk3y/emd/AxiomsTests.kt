package com.github.h0tk3y.emd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class AxiomsTests {
    fun Iterable<BigDecimal>.sum() = fold(BigDecimal.ZERO) { acc, it -> acc + it }
    fun max(x: BigDecimal, y: BigDecimal) = if (x > y) x else y

    val iterations = 1000
    val histogramSize = 20
    val maxKey = 100

    val d = BigDecimalEmd<Int> { x, y -> (BigDecimal(x) - BigDecimal(y)).abs() }

    @Test fun symmetry() {
        repeat(iterations) {
            val h1 = random.intsSequence().take(histogramSize).associateBy ({ it }, { BigDecimal(random.nextInt(100)) })
            val h2 = random.intsSequence().take(histogramSize).associateBy ({ it }, { BigDecimal(random.nextInt(100)) })

            val r1 = d.histogramDistance(h1, h2)
            val r2 = d.histogramDistance(h2, h1)

            assertEquals(r1, r2)
        }
    }

    @Test fun triangle() {
        val weight = 1000

        fun mapWithSum(targetSum: BigDecimal) = random.intsSequence(maxKey).distinct().take(histogramSize)
                .map { it to BigDecimal(random.nextInt(weight / histogramSize)) }
                .toMap(HashMap())
                .apply {
                    var deficiency = targetSum - values.sum()
                    for (entry in this) {
                        val v = entry.value
                        entry.setValue(max(BigDecimal.ZERO, v + deficiency))
                        if (v + deficiency < BigDecimal.ZERO)
                            deficiency += v
                        else
                            deficiency = BigDecimal.ZERO
                        if (deficiency == BigDecimal.ZERO) break
                    }
                }

        repeat(iterations) {
            val h1 = mapWithSum(BigDecimal(weight))
            val h2 = mapWithSum(BigDecimal(weight))
            val h3 = mapWithSum(BigDecimal(weight))

            val r12 = d.histogramDistance(h1, h2)
            val r13 = d.histogramDistance(h1, h3)
            val r23 = d.histogramDistance(h2, h3)

            assertTrue(r12 + r23 >= r13)
            assertTrue(r13 + r23 >= r12)
            assertTrue(r12 + r13 >= r23)
        }
    }
}