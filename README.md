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


### RDDs of key/value pairs: Pair RDDs

```scala
scala> val rdd = sc.parallelize(List((1, 2), (3, 4), (3, 6)))
rdd: org.apache.spark.rdd.RDD[(Int, Int)] = ParallelCollectionRDD[0] at parallelize at <console>:24
```

```scala
scala> rdd.reduceByKey((x, y) => x + y).collect
res0: Array[(Int, Int)] = Array((1,2), (3,10))

scala> rdd.groupByKey.collect
res2: Array[(Int, Iterable[Int])] = Array((1,CompactBuffer(2)), (3,CompactBuffer(4, 6)))

scala> rdd.mapValues(_+1).collect
res3: Array[(Int, Int)] = Array((1,3), (3,5), (3,7))

scala> rdd.flatMapValues(_ to 5).collect
res4: Array[(Int, Int)] = Array((1,2), (1,3), (1,4), (1,5), (3,4), (3,5))

scala> rdd.keys
res5: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[6] at keys at <console>:27

scala> rdd.keys.collect
res6: Array[Int] = Array(1, 3, 3)

scala> rdd.values.collect
res7: Array[Int] = Array(2, 4, 6)

scala> rdd.sortByKey()
res11: org.apache.spark.rdd.RDD[(Int, Int)] = ShuffledRDD[11] at sortByKey at <console>:27

scala> rdd.sortByKey().collect
res12: Array[(Int, Int)] = Array((1,2), (3,4), (3,6))

scala> val other = sc.parallelize(List((3,9)))
other: org.apache.spark.rdd.RDD[(Int, Int)] = ParallelCollectionRDD[16] at parallelize at <console>:24

scala> rdd.subtractByKey(other).collect
res15: Array[(Int, Int)] = Array((1,2))

scala> rdd.join(other)
res16: org.apache.spark.rdd.RDD[(Int, (Int, Int))] = MapPartitionsRDD[20] at join at <console>:29

scala> rdd.join(other).collect
res17: Array[(Int, (Int, Int))] = Array((3,(4,9)), (3,(6,9)))

scala> rdd.rightOuterJoin(other).collect
res18: Array[(Int, (Option[Int], Int))] = Array((3,(Some(4),9)), (3,(Some(6),9)))

scala> rdd.leftOuterJoin(other).collect
res19: Array[(Int, (Int, Option[Int]))] = Array((1,(2,None)), (3,(4,Some(9))), (3,(6,Some(9))))

scala> rdd.cogroup(other).collect
res20: Array[(Int, (Iterable[Int], Iterable[Int]))] = Array((1,(CompactBuffer(2),CompactBuffer())), (3,(CompactBuffer(4, 6),CompactBuffer(9))))
```


#### Aggregations:
They are transformations, return another RDD.
Examples:
- reduceByKey
- foldByKey
```scala

```
```scala

```
```scala

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