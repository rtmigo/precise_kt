package io.github.rtmigo.precise

import kotlin.math.abs

fun <T> List<T>.cascadeSumOf(
    start: Int = 0,
    end: Int = this.size - 1,
    selector: (T) -> Double,
): Double {
    // https://en.wikipedia.org/wiki/Pairwise_summation

    assert(start <= end)
    val len = end - start + 1
    assert(len >= 0)

    return if (len <= 3)
        this.naiveSumOfFragment(start, end, selector)
    else {
        val m = len shr 1

        val endA = start + m
        val startB = endA + 1

        assert(start < endA)
        assert(endA < startB)
        assert(startB < end)

        this.cascadeSumOf(start, endA, selector) +
            this.cascadeSumOf(startB, end, selector)
    }

}

private inline fun <T> List<T>.kahanSumOfFragment(
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

private inline fun <T> List<T>.kleinSumOfFragment(
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


private inline fun <T> List<T>.naiveSumOfFragment(
    start: Int = 0,
    end: Int = this.size,
    selector: (T) -> Double,
): Double {
    var result = 0.0
    for (i in start..end)
        result += selector(this[i])
    return result
}