# Spark Notes

## Key Concepts

Resilient distributed datasets (RDDs): distributed collections that are automatically parallelized across a cluster.
Example in the Scala shell:
```scala
scala> val lines = sc.textFile("README.md")
lines: org.apache.spark.rdd.RDD[String] = README.md MapPartitionsRDD[1] at textFile at <console>:24

scala> lines.count()
res0: Long = 104

scala> lines.first()
res1: String = # Apache Spark

scala> val scalaLines = lines.filter(_.contains("Scala"))
scalaLines: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[4] at filter at <console>:26

scala> scalaLines.first()
res2: String = high-level APIs in Scala, Java, Python, and R, and an optimized engine that
```
where:
- lines is an RDD
- sc is a SparkContext

Driver Program: launches parallel operations on a cluster.
Worked Nodes: contains Executors, which are managed by the former.
