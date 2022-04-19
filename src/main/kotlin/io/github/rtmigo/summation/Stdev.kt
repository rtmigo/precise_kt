/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm Galkin <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

package io.github.rtmigo.summation


import kotlin.math.sqrt

data class StdMean(val std: Double, val mean: Double)

/**
 * Calculate the standard deviation while compensating for floating point summation errors.
 **/
inline fun <T> Collection<T>.accurateStdMeanOf(crossinline selector: (T) -> Double): StdMean {
    if (this.isEmpty()) {
        return StdMean(0.0, 0.0)
    }

    val mean = this.accurateMeanOf(selector)
    val squaredDiffs = this.accurateSumOf {
        val x = selector(it) - mean
        x * x
    }
    val std = sqrt(squaredDiffs / this.size)
    return StdMean(std, mean)
}

@Deprecated("Obsolete", ReplaceWith("accurateStdMeanOf.std"))
fun Collection<Double>.accurateStdev(): Double = this.accurateStdMeanOf { it }.std