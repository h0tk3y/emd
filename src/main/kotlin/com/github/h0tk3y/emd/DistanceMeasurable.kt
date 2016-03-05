package com.github.h0tk3y.emd

/**
 * Provides a conventional way of calculating [TDistance]-distance to items of [TOther].
 * Implementations should follow the traditional metric contract.
 */
interface DistanceMeasurable<TOther, TDistance> {

    /**
     * Get distance to [other].
     * @param other item to calculate distance to
     */
    infix fun distanceTo(other: TOther): TDistance
}