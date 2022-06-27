import sbtassembly.AssemblyPlugin.autoImport.assembly

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"


val akkaHttp = "10.2.9"
val circe = "0.14.1"
val akkaHttpCirce = "1.39.2"
val akkaV = "2.6.9"
val quillV = "3.12.0"
val scalaLoggingV = "3.9.2"
val logbackV      = "1.2.3"



libraryDependencies ++= Seq(
  "ch.qos.logback"    % "logback-classic"   % logbackV,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaV,
  "com.typesafe.akka" %% "akka-http" % akkaHttp,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirce,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "io.circe" %% "circe-generic" % circe,
  "io.getquill" %% "quill-async-postgres" % quillV,
  "com.typesafe.scala-logging" %% "scala-logging"           % scalaLoggingV,

  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttp % "test"
)

assembly / assemblyMergeStrategy := {
  case PathList("application.conf")  => MergeStrategy.concat
  case PathList("reference.conf")    => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}
assembly / assemblyJarName := s"${name.value}-${version.value}.jar"

lazy val root = (project in file("."))
  .settings(
    name := "Pokemon"
  )

