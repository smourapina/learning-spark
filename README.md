# Spark Notes

I will here document my learnings of Spark.

Sources:

[Learning Spark](http://shop.oreilly.com/product/0636920028512.do)



## Key Concepts

Resilient distributed datasets (RDDs): distributed collections that are automatically parallelized across a cluster. Each one is split into multiple partitions, which can be computed by the different nodes in a cluster.
RDDs are computed in a lazy way, and are recomputed every time we perform an action (use persist or cache to save intermediate ones).

Example in the Scala shell:
```scala
scala> val lines = sc.textFile("README.md") //create input RDD from external data
lines: org.apache.spark.rdd.RDD[String] = README.md MapPartitionsRDD[1] at textFile at <console>:24

scala> lines.persist //persist intermediate RDDs to be reused, or cache
res0: lines.type = README.md MapPartitionsRDD[1] at textFile at <console>:24

scala> lines.count() //action
res1: Long = 49

scala> lines.first() //action
17/02/19 13:10:17 WARN Executor: 1 block locks were not released by TID = 2:
[rdd_1_0]
res2: String = # Spark Notes

scala> val scalaLines = lines.filter(_.contains("Scala")) //transformation, returns a new RDD
scalaLines: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[4] at filter at <console>:26

scala> scalaLines.first() //action, returns a result or writes to storage
res2: String = high-level APIs in Scala, Java, Python, and R, and an optimized engine that
```
where:
- lines is an RDD
- sc is a SparkContext

Driver Program: launches parallel operations on a cluster.

Worked Nodes: contains Executors, which are managed by the former.


## RDDs
Creating RDDs:
```scala
scala> val lines = sc.parallelize(List("spark", "there is a spark"))
lines: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[2] at parallelize at <console>:24

scala> val text = sc.textFile("README.md")
text: org.apache.spark.rdd.RDD[String] = README.md MapPartitionsRDD[4] at textFile at <console>:24
```

Transformations:
RDDs transformations are computed lazily, only when used in an action. Returns a pointer to a new RDD.
```scala
scala> val filtered = text.filter(_.contains("scala"))
filtered: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[5] at filter at <console>:26
```
Element-wise transformations: filter, map, flatMap
Pseudo set operations: distinct (requires shuffling data!), union(other), intersection(other), subtract(other), cartesian(other), sample(withReplacement, fraction)

Actions:
```scala
scala> val nums = sc.parallelize(List(1, 2, 3, 3))
nums: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[6] at parallelize at <console>:24

scala> nums.collect
res10: Array[Int] = Array(1, 2, 3, 3)

scala> nums.count
res11: Long = 4

scala> nums.countByValue
res13: scala.collection.Map[Int,Long] = Map(1 -> 1, 2 -> 1, 3 -> 2)

scala> nums.take(2)
res14: Array[Int] = Array(1, 2)

scala> nums.top(2)
res15: Array[Int] = Array(3, 3)

scala> nums.takeSample(false, 1)
res16: Array[Int] = Array(3)

scala> nums.reduce((x, y) => x * y)
res17: Int = 18

scala> nums.fold(1)((x, y) => x * y)
res18: Int = 18

scala> nums.aggregate((0, 0))((x, y) => (x._1 + y, x._2 + 1), (x, y) => (x._1 + y._1, x._2 + y._2))
res19: (Int, Int) = (9,4)
```

Converting between RDD types:
Use implicit conversions by importing a SparkContext. This turns an RDD into a wrapper class. Needed to use functions like mean and variance (only exist for numeric values).
Example: an RDD[Double] is converted to DoubleRDDFunctions.
```scala
import org.apache.spark.SparkContext._
```

Persistence (caching):
Do it before calling first action!
```scala
import org.apache.spark.storage.StorageLevel

val result = input.map(_ * _)
result.persist(StorageLevel.DISK_ONLY)
```

```scala

```

## Code examples

How to run:
```scala
sbt clean package
```
```scala
SPARK_HOME/bin/spark-submit --class learningsparkexamples.singlemachine.WordCount ./target/scala-2.11/learning-spark-single-machine_2.11-0.0.1.jar ./README.md ./wordcounts
```