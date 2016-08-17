name := "scala-credstash"
version := "1.0"
scalaVersion := "2.11.8"
organization := "au.com.simplemachines"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.26",
  "com.amazonaws" % "aws-java-sdk-kms" % "1.11.26"
)

