package com.github.h0tk3y.emd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class EmdTests {
    val iterations = 1000

    @Test fun constantZeroDistance() {
        val d: HistogramDistance<Int, Int> = IntEmd { x, y -> 0 }
        repeat(iterations) {
            val h1 = random.intsSequence().take(5).associateBy { random.nextInt() }
            val h2 = random.intsSequence().take(5).associateBy { random.nextInt() }
            val result = d.histogramDistance(h1, h2)

            assertEquals(0, result)
        }
    }

    @Test fun sameHistograms() {
        val d: Emd<Int, BigDecimal> = BigDecimalEmd { x, y -> BigDecimal(Math.abs(x - y)) }
        repeat(iterations) {
            val h1 = random.intsSequence().take(5).associateBy({ it }, { BigDecimal(Math.abs(random.nextInt(100))) })
            val result = d.histogramDistance(h1, h1)

            assertEquals(BigDecimal.ZERO, result)
        }
    }

    @Test fun customDistance() {
        repeat(iterations) {
            val factor = random.nextInt(100) + 1
            val d = IntEmd<Int> { x, y -> Math.abs(x - y) * factor }
            val h1 = mapOf(0 to 1)
            val h2 = mapOf(1 to 1)
            val result = d.histogramDistance(h1, h2)

            assertEquals(factor, result)
        }
    }

    @Test fun naturalDistance() {
        var distanceCalled = false

        class Point(val x: Int, val y: Int) : DistanceMeasurable<Point, Int> {
            override fun distanceTo(other: Point): Int {
                distanceCalled = true
                return Math.abs(x - other.x) + Math.abs(y - other.y)
            }
        }

        val d = IntEmd<Point>()
        val h1 = mapOf(Point(0, 0) to 1)
        val h2 = mapOf(Point(1, 1) to 1)
        val result = d.histogramDistance(h1, h2)

        assertTrue(distanceCalled)
        assertEquals(2, result)
    }
}