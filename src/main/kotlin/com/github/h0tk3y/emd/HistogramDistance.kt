package com.github.h0tk3y.emd

/**
 Provides histogram distance for histograms with [TItems] keys and [TNumeric] values.

 Implementations should follow the traditional metric contract:

 1. d(a, b) >= 0; d(a, b) = 0 iff a = b.
 2. d(a, b) = d(b, a)
 3. d(a, b) + d(b, c) <= d(a, c)
 */
interface HistogramDistance<TItems, TNumeric : Number> {

    /**
     * Calculates distance between histograms [h1] and [h2].
     *
     * @param h1 first map in pair
     * @param h2 second map in pair
     * @return Distance between the maps of the same type [TNumeric] to the maps values.
     */
    fun histogramDistance(h1: Map<TItems, TNumeric>, h2: Map<TItems, TNumeric>): TNumeric
}