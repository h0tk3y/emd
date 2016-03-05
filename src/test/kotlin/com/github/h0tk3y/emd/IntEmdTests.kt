package com.github.h0tk3y.emd

import org.junit.Assert
import org.junit.Test
import java.util.*

class IntEmdTests {
    val iterations = 1000
    val histogramSize = 10
    val d = IntEmd<Int> { x, y -> Math.abs(x - y) }

    @Test fun symmetry() {
        repeat(iterations) {
            val h1 = random.intsSequence(100).take(histogramSize).associateBy ({ it }, { random.nextInt(10) })
            val h2 = random.intsSequence(100).take(histogramSize).associateBy ({ it }, { random.nextInt(10) })

            val r1 = d.histogramDistance(h1, h2)
            val r2 = d.histogramDistance(h2, h1)

            Assert.assertEquals(r1, r2)
        }
    }

    @Test fun triangle() {
        fun mapWithSum(targetSum: Int) = random.intsSequence(10).distinct().take(histogramSize)
                .map { Pair(random.nextInt(100), random.nextInt(100)) }
                .toMap(HashMap())
                .apply {
                    val sum = values.sum()
                    compute(keys.first()) { k, v -> v + targetSum - sum }
                }

        repeat(iterations) {
            val targetSum = random.nextInt(1000) + 1000
            val h1 = mapWithSum(targetSum)
            val h2 = mapWithSum(targetSum)
            val h3 = mapWithSum(targetSum)

            val r12 = d.histogramDistance(h1, h2)
            val r13 = d.histogramDistance(h1, h3)
            val r23 = d.histogramDistance(h2, h3)

            try {
                Assert.assertTrue(r12 + r23 >= r13)
                Assert.assertTrue(r13 + r23 >= r12)
                Assert.assertTrue(r12 + r13 >= r23)
            } catch (e: AssertionError) {
                println(h1)
                println(h2)
                println(h3)
                Assert.fail()
            }
        }
    }
}