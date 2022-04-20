/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm IG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

package io.github.rtmigo.precise

import kotlin.math.abs

fun <T> List<T>.cascadeSumOf(
    start: Int = 0,
    end: Int = this.size - 1,
    selector: (T) -> Double,
): Double {
    // https://en.wikipedia.org/wiki/Pairwise_summation
    // todo unit test

    val len = end - start + 1

    return if (len <= 3)
        this.sublistSumOf(start, end, selector)
    else {
        val m = len shr 1

        // sample calculation (splitting 20..23):
        //      start = 20
        //      end = 23
        //      len = 23-20+1 = 4
        //      m = 2
        //      endA = 20+2-1 = 21
        //      startB = 21+1 = 22
        // so we end with two intervals 20..21 and 22..23

        val endA = start + m - 1
        val startB = endA + 1

        assert(start < endA)
        assert(endA < startB)
        assert(startB < end)
        // two halves have same size or differ by 1
        assert(abs((endA-start)-(end-startB))<=1)

        this.cascadeSumOf(start, endA, selector) +
            this.cascadeSumOf(startB, end, selector)
    }
}

private inline fun <T> List<T>.sublistKahanSumOf(
    start: Int = 0,
    end: Int = this.size,
    selector: (T) -> Double,
): Double {
    // https://rosettacode.org/wiki/Kahan_summation#Kotlin
    var sum = 0.0
    var c = 0.0

    for (i in start..end) {
        val f = this[i]
        val y = selector(f) - c
        val t = sum + y
        c = (t - sum) - y
        sum = t
    }
    return sum
}

private inline fun <T> List<T>.sublistKleinSumOf(
    start: Int = 0,
    end: Int = this.size,
    selector: (T) -> Double,
): Double {

    var sum = 0.0
    var cs = 0.0
    var ccs = 0.0

    for (i in start..end) {
        //for (item in this) {

        val input = selector(this[i])

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


private inline fun <T> List<T>.sublistSumOf(
    start: Int = 0,
    end: Int = this.size,
    selector: (T) -> Double,
): Double {
    var result = 0.0
    for (i in start..end)
        result += selector(this[i])
    return result
}