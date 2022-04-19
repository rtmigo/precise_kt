/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm Galkin <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

package io.github.rtmigo.summation

import kotlin.math.abs

/**
 * Drop-in replacement for [sumOf]. Compensated summation algorithm reduces numerical error when
 * summing sequences of [Double].
 *
 * The algorithm is described [A Generalized Kahan-Babuška-Summation-Algorithm](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.582.288&rep=rep1&type=pdf)
 * by A. Klein (2005), page 6 ("second order algorithm").
 *
 * @see [kahanSumOf]
 * @see [MutableAccurateSum]
 * @see [AccurateSum]
 **/
@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("accurateSumOfDouble")
inline fun <T> Iterable<T>.accurateSumOf(selector: (T) -> Double): Double {
/*
    https://en.wikipedia.org/wiki/Kahan_summation_algorithm
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
 * @see [AccurateSum]
 * @see [accurateSumOf]
 **/
class MutableAccurateSum(
    var sum: Double = 0.0,
    internal var cs: Double = 0.0,
    internal var ccs: Double = 0.0,
    internal var c: Double = 0.0,
    internal var cc: Double = 0.0
) {
    fun add(x: Iterable<Double>) =
        x.forEach { this.add(it) }

    fun add(x: Double) {

        var t = sum + x

        if (abs(sum) >= abs(x)) {
            c = (sum - t) + x
        }
        else {
            c = (x - t) + sum
        }

        sum = t
        t = cs + c
        if (abs(cs) >= abs(c)) {
            cc = (cs - t) + c
        }
        else {
            cc = (c - t) + cs
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
            c = 0.0
            cc = 0.0
        }

    //fun toDouble() = this.value
}

/**
 * Immutable version of running compensated summation algorithm.
 *
 *      var sum = KleinSum()
 *      sum += 1.0
 *      sum += listOf(2.3, 4.5)
 *      println(sum.toDouble())
 *
 * This is slightly slower than [MutableAccurateSum] because of the need to instantiate the class
 * on changes.
 *
 * The algorithm is described [A Generalized Kahan-Babuška-Summation-Algorithm](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.582.288&rep=rep1&type=pdf)
 * by A. Klein (2005), page 6 ("second order algorithm").
 *
 * @see [MutableAccurateSum]
 * @see [accurateSumOf]
 **/
data class AccurateSum(
    private val sum: Double = 0.0,
    private val cs: Double = 0.0,
    private val ccs: Double = 0.0,
    private val c: Double = 0.0,
    private val cc: Double = 0.0
) {
    operator fun plus(x: Iterable<Double>): AccurateSum =
        MutableAccurateSum(
            sum = sum,
            cs = cs,
            ccs = ccs,
            c = c,
            cc = cc
        ).let {
            it.add(x)

            AccurateSum(
                sum = it.sum,
                cs = it.cs,
                ccs = it.ccs,
                c = it.c,
                cc = it.cc
            )
        }

    operator fun plus(x: Double): AccurateSum {
        var t = sum + x

        val newC = if (abs(sum) >= abs(x)) {
            (sum - t) + x
        }
        else {
            (x - t) + sum
        }

        val newSum = t
        t = cs + newC
        val newCC = if (abs(cs) >= abs(newC)) {
            (cs - t) + newC
        }
        else {
            (newC - t) + cs
        }
        val newCs = t
        val newCcs = ccs + newCC

        return copy(c = newC, sum = newSum, cs = newCs, ccs = newCcs)
    }

    val value: Double = sum + cs + ccs

    //fun toDouble() = value

    operator fun minus(x: Double): AccurateSum = plus(-x)
    operator fun minus(x: Iterable<Double>): AccurateSum = plus(x.map { -it })
}

@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun <T> Collection<T>.accurateMeanOf(crossinline selector: (T) -> Double): Double =
    this.meanByFuncOf(Iterable<T>::accurateSumOf, selector)