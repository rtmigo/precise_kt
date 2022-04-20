/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm IG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

package io.github.rtmigo.precise


import kotlin.math.sqrt

data class StdMean(val std: Double, val mean: Double)

/**
 * Calculate the standard deviation while compensating for floating point summation errors.
 **/
inline fun <T> Collection<T>.preciseStdMeanOf(crossinline selector: (T) -> Double): StdMean {
    if (this.isEmpty()) {
        return StdMean(0.0, 0.0)
    }

    val mean = this.preciseMeanOf(selector)
    val squaredDiffs = this.preciseSumOf {
        val x = selector(it) - mean
        x * x
    }
    val std = sqrt(squaredDiffs / this.size)
    return StdMean(std, mean)
}

@Deprecated("Obsolete", ReplaceWith("preciseStdMeanOf.std"))
fun Collection<Double>.preciseStdev(): Double = this.preciseStdMeanOf { it }.std