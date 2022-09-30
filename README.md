![Generic badge](https://img.shields.io/badge/maturity-wip-red.svg)
![Generic badge](https://img.shields.io/badge/JVM-8-blue.svg)

# [precise](https://github.com/rtmigo/precise_kt#readme)

Implements [compensated summation](https://en.wikipedia.org/wiki/Kahan_summation_algorithm)
for sequences of `Double`. Reduces rounding errors associated with limited
precision of floating-point numbers.

```kotlin
val numbers = List(420) { 0.1 }  // 420 x 0.01

numbers.preciseSumOf { it } // 42.0 (compensated sum)
numbers.sumOf { it }        // 42.00000000000033 (naive sum)
```

The table shows the total error when summing the same sequence of random
numbers. All the terms were rounded to 0.0001 before addition. In the **%**
column, the error of `preciseSumOf` compared to `sumOf`.

| Terms     | err( sum )    | err( preciseSum ) | %      |
|-----------|---------------|-------------------|--------|
| 10        | 0.00000000003 | 0.00000000003     | 100.0% |
| 100       | 0.0000000008  | 0.00000000002     | 3.03%  |
| 1,000     | 0.000000001   | 0.0000000001      | 9.57%  |
| 10,000    | 0.00000002    | 0.0000000007      | 3.57%  |
| 100,000   | 0.0000005     | 0.000000004       | 0.77%  |
| 1,000,000 | 0.000009      | 0.000000003       | 0.03%  |

% is err(preciseSum) / err(sum)

Most of the functions use "second-order iterative Kahan–Babuška algorithm"
by [Klein (2005)](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.582.288&rep=rep1&type=pdf)
.

# Install

#### settings.gradle.kts

```kotlin
sourceControl {
    gitRepository(java.net.URI("https://github.com/rtmigo/precise_kt.git")) {
        producesModule("io.github.rtmigo:precise")
    }
}
```

#### build.gradle.kts

```kotlin
dependencies {
    implementation("io.github.rtmigo:precise") {
        version { branch = "staging" }
    }
}
```

#### Import in Kotlin code:

```kotlin
import io.github.rtmigo.precise.*  // Kotlin
```

# Lambda functions

```kotlin
val sequence = listOf(1, 2, 3)

// sum
sequence.preciseSumOf { it * 0.1 }  // equals 0.6

// arithmetic mean
sequence.preciseMeanOf { it * 0.1 }  // equals 0.2

// standard deviation and mean
val (stdev, mean) = sequence.preciseStdevMean { it * 0.1 }
```

# Running sum

Running sum, immutable version:

```kotlin
var sum = PreciseSum(5.0)  // 5.0 is optional starting value

sum += 0.1
sum += listOf(0.2, 0.3)
println(sum.value)  // 5.6

sum -= 0.2
println(sum.value)  // 5.4
```

Running sum, mutable version (faster):

```kotlin
val sum = MutablePreciseSum(5.0)  // 5.0 is optional starting value

sum.add(0.1)
sum.add(listOf(0.2, 0.3))
println(sum.value)  // 5.6

sum.add(-0.2)
println(sum.value)  // 5.4
```

# Benchmarks

An alternative to compensated summation is to use BigDecimal: there is no error
when summing them. However, even in the case of a pre-generated array, BigDecimals are 5-10 times slower.

| Method                                     | Time    |
|--------------------------------------------|---------|
| `List<Double>.sumOf` (naive)               | 17 ms   |
| `List<Double>.preciseSumOf`                | 48 ms   |
| `MutablePreciseSum`                        | 50 ms   |
| `PreciseSum` (immutable)                   | 75 ms   |
| `List<BigDecimal>.sumOf`                   | 501 ms  |
| `List<Double>.sumOf { it.toBigDecimal() }` | 3192 ms |

# Other functions

`kahanSumOf` implements
Kahan [compensated summation algorithm](https://en.wikipedia.org/wiki/Kahan_summation_algorithm)
in its traditional form. The accuracy is worse than `preciseSumOf`, but better
than the naive sum.

```kotlin
val sequence = listOf(1, 2, 3)
sequence.kahanSumOf { it * 0.1 }  // 0.6
```

`cascadeSumOf`
performs [pairwise summation](https://en.wikipedia.org/wiki/Pairwise_summation).
The accuracy is worse than `preciseSumOf`, but better than the naive sum.

```kotlin
val sequence = listOf(1, 2, 3)
sequence.cascadeSumOf { it * 0.1 }  // 0.6
```

`welfordMeanOf` calculates the arithmetic mean, avoiding overflow when summing
too large values.

```kotlin
val sequence = listOf(1, 2, 3)
println(sequence.welfordMeanOf { it * 0.1 })  // 0.3
```



## License

Copyright © 2022 [Artsiom iG](https://github.com/rtmigo).
Released under the [MIT License](LICENSE).