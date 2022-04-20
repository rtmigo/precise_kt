/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm IG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

import io.github.rtmigo.precise.welfordMeanOf
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.random.Random

internal class WelfordMeanTest {
    @Test
    fun testWelfordMean() {
        listOf<Double>().welfordMeanOf { it }.shouldBe(0.0)
        listOf(5.0).welfordMeanOf { it }.shouldBe(5.0)
        listOf(1.0, 2.0).welfordMeanOf { it }.shouldBe(1.5)
        listOf(1.0, 2.0, 6.0).welfordMeanOf { it }.shouldBe(3.0)

        val randoms = (1..100).map { Random.nextDouble(-10.0, 10.0) }
        val naive = randoms.sumOf { it } / randoms.size
        val welford = randoms.welfordMeanOf { it }

        abs(welford - naive).shouldBeLessThan(1E-10)
    }

    @Test
    fun `test huge numbers`() {
        val items = (1..555).map { 1E+307 }
        (items.sumOf { it } / items.size).shouldBe(Double.POSITIVE_INFINITY) // oops
        (items.average()).shouldBe(Double.POSITIVE_INFINITY) // oops
        items.welfordMeanOf { it }.shouldBe(1E+307) // whoa!
    }

    @Test
    fun empty() {
        listOf<Int>().welfordMeanOf { 5.0 }.shouldBe(0.0)
    }
}