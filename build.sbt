enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
val tapirVersion = "1.1.4"
val zioVersion = "3.0.2"
val zioTest = "2.0.3"
val doobieVersion = "1.0.0-RC2"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "affirmations",
    version := "0.1.0-SNAPSHOT",
    organization := "com.protolight",
    scalaVersion := "3.2.1",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "org.http4s" %% "http4s-blaze-server" % "0.23.12",
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % "1.3.4", // don't upgrade to 1.4.x, docker won't start nor show logs
      "dev.zio" %% "zio-config" % zioVersion,
      "dev.zio" %% "zio-config-typesafe" % zioVersion,
      "dev.zio" %% "zio-config-magnolia" % zioVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "dev.zio" %% "zio-interop-cats" % "3.3.0", // version 22 is for cats 2 ... and is older than 13.0.0.1 :(
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "dev.zio" %% "zio-test" % zioTest % Test,
      "dev.zio" %% "zio-test-sbt" % zioTest % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.8.3" % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    dockerExposedPorts ++= Seq(8080)
  )
)
