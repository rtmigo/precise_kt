/*
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 */


package io.github.rtmigo.precise

import kotlin.math.abs

/**
 * Drop-in replacement for [sumOf]. Compensated summation algorithm reduces numerical error when
 * summing sequences of [Double].
 *
 * The algorithm is described [A Generalized Kahan-Babuška-Summation-Algorithm](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.582.288&rep=rep1&type=pdf)
 * by A. Klein (2005), page 6 ("second order algorithm").
 *
 * @see [kahanSumOf]
 * @see [MutablePreciseSum]
 * @see [PreciseSum]
 **/
//@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
//@OverloadResolutionByLambdaReturnType
//@JvmName("preciseSumOfDouble")
inline fun <T> Iterable<T>.preciseSumOf(selector: (T) -> Double): Double {
/*
    Original from https://en.wikipedia.org/wiki/Kahan_summation_algorithm
    function KahanBabushkaKleinSum(input)
        var sum = 0.0
        var cs  = 0.0
        var ccs = 0.0
        var c   = 0.0
        var cc  = 0.0

        for i = 1 to input.length do
            var t = sum + input[i]
            if |sum| >= |input[i]| then
                c = (sum - t) + input[i]
            else
                c = (input[i] - t) + sum
            endif
            sum = t
            t = cs + c
            if |cs| >= |c| then
                cc = (cs - t) + c
            else
                cc = (c - t) + cs
            endif
            cs = t
            ccs = ccs + cc
        end loop

        return sum + cs + ccs
*/

    var sum = 0.0
    var cs = 0.0
    var ccs = 0.0

    for (item in this) {
        val input = selector(item)

        var t = sum + input

        val c = if (abs(sum) >= abs(input)) {
            (sum - t) + input
        }
        else {
            (input - t) + sum
        }

        sum = t
        t = cs + c
        val cc = if (abs(cs) >= abs(c)) {
            (cs - t) + c
        }
        else {
            (c - t) + cs
        }
        cs = t
        ccs += cc
    }

    return sum + cs + ccs
}

/**
 * Running compensated summation algorithm.
 *
 *      var sum = MutableKleinSum()
 *      sum.add(1.0)
 *      sum.add(listOf(2.3, 4.5))
 *      println(sum.toDouble())
 *
 * The algorithm is described [A Generalized Kahan-Babuška-Summation-Algorithm](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.582.288&rep=rep1&type=pdf)
 * by A. Klein (2005), page 6 ("second order algorithm").
 *
 * @see [PreciseSum]
 * @see [preciseSumOf]
 **/
class MutablePreciseSum(
    internal var sum: Double = 0.0,
    internal var cs: Double = 0.0,
    internal var ccs: Double = 0.0,
) {
    fun add(x: Iterable<Double>) =
        x.forEach { this.add(it) }

    fun add(x: Double) {
        var t = sum + x

        val c = if (abs(sum) >= abs(x)) {
            (sum - t) + x
        }
        else {
            (x - t) + sum
        }

        sum = t
        t = cs + c
        val cc = if (abs(cs) >= abs(c)) {
            (cs - t) + c
        }
        else {
            (c - t) + cs
        }
        cs = t
        ccs += cc
    }

    /**
     * The current sum value.
     **/
    val value: Double
        get() =
            sum + cs + ccs

    fun reset(x: Double) {
        sum = x
        cs = 0.0
        ccs = 0.0
    }
}

/**
 * Immutable version of running compensated summation algorithm.
 *
 *      var sum = KleinSum()
 *      sum += 1.0
 *      sum += listOf(2.3, 4.5)
 *      println(sum.toDouble())
 *
 * This is slightly slower than [MutablePreciseSum] because of the need to instantiate the class
 * on changes.
 *
 * The algorithm is described [A Generalized Kahan-Babuška-Summation-Algorithm](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.582.288&rep=rep1&type=pdf)
 * by A. Klein (2005), page 6 ("second order algorithm").
 *
 * @see [MutablePreciseSum]
 * @see [preciseSumOf]
 **/
data class PreciseSum(
    private val sum: Double = 0.0,
    private val cs: Double = 0.0,
    private val ccs: Double = 0.0,
) {
    operator fun plus(x: Iterable<Double>): PreciseSum =
        MutablePreciseSum(
            sum = sum,
            cs = cs,
            ccs = ccs,
        ).let { mutable ->

            mutable.add(x)

            PreciseSum(
                sum = mutable.sum,
                cs = mutable.cs,
                ccs = mutable.ccs,
            )
        }

    operator fun plus(x: Double): PreciseSum {
        var t = sum + x

        val c = if (abs(sum) >= abs(x)) {
            (sum - t) + x
        }
        else {
            (x - t) + sum
        }

        val newSum = t
        t = cs + c
        val cc = if (abs(cs) >= abs(c)) {
            (cs - t) + c
        }
        else {
            (c - t) + cs
        }

        return copy(sum = newSum,
                    cs = t,
                    ccs = ccs + cc)
    }

    val value: Double = sum + cs + ccs

    operator fun minus(x: Double): PreciseSum = plus(-x)
    operator fun minus(x: Iterable<Double>): PreciseSum = plus(x.map { -it })
}

//@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
//@OverloadResolutionByLambdaReturnType
inline fun <T> Collection<T>.preciseMeanOf(crossinline selector: (T) -> Double): Double =
    this.meanByFuncOf(Iterable<T>::preciseSumOf, selector)