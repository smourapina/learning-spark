name := "learning-spark-single-machine"

version := "0.0.1"

scalaVersion := "2.11.8"

// additional libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0" % "provided"
)
