/*
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 */


package io.github.rtmigo.precise

import kotlin.math.sqrt

data class StdevMean(val stdev: Double, val mean: Double)

/**
 * Calculate the standard deviation while compensating for floating point summation errors.
 **/
inline fun <T> Collection<T>.preciseStdevMeanOf(crossinline selector: (T) -> Double): StdevMean {
    if (this.isEmpty()) {
        return StdevMean(0.0, 0.0)
    }

    val mean = this.preciseMeanOf(selector)
    val squaredDiffs = this.preciseSumOf {
        val x = selector(it) - mean
        x * x
    }
    val std = sqrt(squaredDiffs / this.size)
    return StdevMean(std, mean)
}

@Deprecated("Obsolete", ReplaceWith("preciseStdevMeanOf.stdev"))
fun Collection<Double>.preciseStdev(): Double = this.preciseStdevMeanOf { it }.stdev