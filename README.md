EMD
===
[![](https://jitpack.io/v/h0tk3y/emd.svg)](https://jitpack.io/#h0tk3y/emd)

Implementation of 
[Earth Mover's Distance](http://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/RUBNER/emd.htm)
for histograms in Kotlin.

It uses successive shortest path min-cost 
max-flow solution with [SPFA](https://www.wikiwand.com/en/Shortest_Path_Faster_Algorithm) inside and thus is quite fast.

Current implementation works for histograms with equal weight sums. If the weights are different, it behaves as if some mass can be dropped, and only the minimal of two weight sums is necessary to move.

How to add a dependency
---

    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
 
    dependencies {
        compile 'com.github.h0tk3y:emd:v0.9'
    }

How to use
---
Use either `BigDecimalEmd` or `IntEmd`, based on the numeric type you use. 
Note that `IntEmd` is not protected from numerical overflow.
You can also make an `Emd` implementation for your own numeric type, but remember that it should be precise 
(that's why there's no `DoubleEmd`).
 
First, implement the distance calculation for your type. In this example, let it be `Point`. There are two ways of doing it:
 
* provide your distance as a function to the constructor:

   ```kotlin
   val d = BigDecimalEmd<Point> { a, b -> 
       BigDecimal(Math.sqrt((a.x - b.x).let { it * it} + (a.y - b.y).let { it * it}))
   }
   ```
    
* or implement `DistanceMeasurable` interface for your class:
 
   ```kotlin
   class Point: DistanceMeasurable<Point> {
       /* class code */
       
       override fun distanceTo(other: Point) = 
           BigDecimal(Math.sqrt((a.x - b.x).let { it * it} + (a.y - b.y).let { it * it}))
   }
   
   val d = BigDecimalEmd<Point>() //no argument
   ```
   
Then you can call `histogramDistance` on two maps (here, maps from `Point` to `BigDecimal`):
 
```kotlin
val m1 = mapOf(Point(0.0, 1.1) to BigDecimal(2.0), Point(1.1, 0.0) to BigDecimal(8.0))
val m2 = mapOf(Point(0.5, 0.5) to BigDecimal(10.0))
 
val result = d.histogramDistance(m1, m2)
```
