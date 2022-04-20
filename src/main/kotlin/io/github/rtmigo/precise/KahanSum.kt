/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm iG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

package io.github.rtmigo.precise

/**
 * Kahan summation algorithm, also known as compensated summation, significantly reduces the
 * numerical error in the total obtained by adding a sequence of finite-precision floating-point
 * numbers, compared to the obvious approach.
 **/
@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("kahanSumOfDouble")
inline fun <T> Iterable<T>.kahanSumOf(selector: (T) -> Double): Double {
    // https://rosettacode.org/wiki/Kahan_summation#Kotlin
    var sum = 0.0
    var c = 0.0
    for (f in this) {
        val y = selector(f) - c
        val t = sum + y
        c = (t - sum) - y
        sum = t
    }
    return sum
}



@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun <T> Collection<T>.kahanMeanOf(crossinline selector: (T) -> Double): Double =
    this.meanByFuncOf(Iterable<T>::kahanSumOf, selector)

inline fun <T> Collection<T>.meanByFuncOf(
    customSumOf: Iterable<T>.((T) -> Double) -> Double,
    crossinline selector: (T) -> Double,
): Double =
    if (this.isEmpty())
        0.0
    else
        this.customSumOf { selector(it) } / this.size

