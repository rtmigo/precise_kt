

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class StdevTest {
    @Test
    fun stdmean() {
        val (std,mean) = listOf(2, 4, 4, 4, 5, 5, 7, 9).accurateStdMeanOf { it.toDouble()/100 }
        std.shouldBe(0.02)
        mean.shouldBe(0.05)
    }

    @Test
    fun empty() {
        val (std,mean) = listOf<Int>().accurateStdMeanOf { it.toDouble()/100 }
        std.shouldBe(0.0)
        mean.shouldBe(0.0)
    }
}