

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.random.Random

fun randomDecimalString(r: Random = Random): String {
    val maxLength = 16
    val length = r.nextInt(2,maxLength)
    val chars = (0..length).map { "0123456789".random(r) }
    assert(chars.size in 2..maxLength)
    val dotPos = r.nextInt(1, chars.size-1)
    val mutableChars = chars.toMutableList()
    mutableChars.add(dotPos, '.')
    return mutableChars.joinToString("")
}

class RandomDecimalTest {
    @Test
    fun test() {
        (1..5).map { randomDecimalString(Random(it)) }.shouldBe(
            listOf("6289717.015", "907197.51526346", "53.609", "89554.04", "64.46662915")
        )
    }
}