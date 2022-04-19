![Generic badge](https://img.shields.io/badge/maturity-experimental-red.svg)
![Generic badge](https://img.shields.io/badge/JVM-8-blue.svg)
![JaCoCo](https://raw.github.com/rtmigo/dec_kt/dev_updated_by_actions/.github/badges/jacoco.svg)

# io.github.rtmigo : [summation](https://github.com/rtmigo/summation_kt#readme)

## Compensated summation

[Compensated summation](https://en.wikipedia.org/wiki/Kahan_summation_algorithm)
reduces numerical error when summing sequences of `Double`.

This does not eliminate rounding errors, but reduces them by orders of magnitude.

```kotlin
val numbers = listOf(0.1, 0.2, 0.1, 0.2, 0.1, 0.2, 0.1)

numbers.sumOf { it }         // 1.0000000000000002 (naive)
numbers.accurateSumOf { it } // 1.0 (compensated)
```

### Running compensated sum

The following functions implement "second-order iterative Kahan–Babuška algorithm" suggested by [Klein (2005)](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.582.288&rep=rep1&type=pdf).

Running sum, immutable version:

```kotlin
var sum = AccurateSum(5.0)  // 5.0 is optional starting value

sum += 0.1               // create new object and reassign var
sum += listOf(0.2, 0.3)  // create new object and reassign var

println(sum.value)       // 5.6

sum -= 0.2               // create new object and reassign var

println(sum.value)       // 5.4
```

Running sum, mutable version (faster):

```kotlin
val sum = MutableAccurateSum(5.0)  // 5.0 is optional starting value

sum.add(0.1)                    // mutate the sum object
sum.add(listOf(0.2, 0.3))       // mutate the sum object

println(sum.value)              // 5.6

sum.add(-0.2)                   // mutate the sum object

println(sum.value)              // 5.4
```

### Lambda functions with compensated sums

```kotlin
val sequence = listOf(1, 2, 3)

// sum
sequence.accurateSumOf { it * 0.1 }  // equals 0.6

// arithmetic mean
sequence.accurateMeanOf { it * 0.1 }  // equals 0.2

// standard deviation and mean
val (stdev, mean) = sequence.accurateStdMeanOf { it * 0.1 }
```

### Kahan compensated summation

`kahanSumOf` implements Kahan [compensated summation algorithm](https://en.wikipedia.org/wiki/Kahan_summation_algorithm)
in its traditional form.

The calculation accuracy is slightly lower than `accurateSumOf` (by Klein),
but still much better than the naive sum.

```kotlin
val sequence = listOf(1, 2, 3)
println( sequence.kahanSumOf { it * 0.1 } )  // 0.6
```

## Welford

Calculates the arithmetic mean, avoiding overflow when summing too large
values.

```kotlin
val sequence = listOf(1, 2, 3)
println( sequence.welfordMeanOf { it * 0.1 } )  // 0.3
```

# Install

#### settings.gradle.kts

```kotlin
sourceControl {
    gitRepository(java.net.URI("https://github.com/rtmigo/summation_kt.git")) {
        producesModule("io.github.rtmigo:summation")
    }
}
```

#### build.gradle.kts

```kotlin
dependencies {
    implementation("io.github.rtmigo:summation")
}
```