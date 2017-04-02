import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

name := "scala-credstash"
version := "1.01"
scalaVersion := "2.11.8"
organization := "io.policarp"
homepage := Some(url("https://github.com/kdrakon/scala-credstash"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/kdrakon/scala-credstash"),
    "scm:git@github.com:kdrakon/scala-credstash.git"
  )
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.26",
  "com.amazonaws" % "aws-java-sdk-kms" % "1.11.26"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)

pomIncludeRepository := { _ => false }
publishMavenStyle := true
licenses := Seq("Apache License 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
developers := List(
  Developer(
    id    = "kdrakon",
    name  = "Sean Policarpio",
    email = "",
    url   = url("http://policarp.io")
  )
)
useGpg := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

