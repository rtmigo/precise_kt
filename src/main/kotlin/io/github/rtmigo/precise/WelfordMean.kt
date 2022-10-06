/*
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 */

@file:JvmName("Precise")
@file:JvmMultifileClass
package io.github.rtmigo.precise

/**
 * Calculates the arithmetic mean, avoiding overflow when summing too large
 * values. However, there is a risk when summing too small (underflow).
 *
 * The method is popularized in "The Art of Computer Programming" by D.Knuth.
 **/
//@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
//@OverloadResolutionByLambdaReturnType
inline fun <T> Iterable<T>.welfordMeanOf(selector: (T) -> Double): Double {
    // Я взял алгоритм отсюда: https://stackoverflow.com/a/1934266/11700241
    // Описание нашёл и здесь: https://nullbuffer.com/articles/welford_algorithm.html
    //
    // Я заменил сумму на компенсационную.
    // Также алгоритм Велфорда умеет считать стандартное отклонение. Этого я не делал.
    // Ещё алгоритм можно превратить в "бегущее среднее" (возвращать сумму на каждом шаге).


    val sum = MutablePreciseSum()

    for ((i, x) in this.withIndex()) {
        sum.add((selector(x) - sum.value) / (i + 1))
    }
    return sum.value
}
