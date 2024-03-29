/*
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 */


import io.github.rtmigo.precise.preciseStdevMeanOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class StdevTest {
    @Test
    fun stdmean() {
        val (std,mean) = listOf(2, 4, 4, 4, 5, 5, 7, 9).preciseStdevMeanOf { it.toDouble()/100 }
        std.shouldBe(0.02)
        mean.shouldBe(0.05)
    }

    @Test
    fun empty() {
        val (std,mean) = listOf<Int>().preciseStdevMeanOf { it.toDouble()/100 }
        std.shouldBe(0.0)
        mean.shouldBe(0.0)
    }
}