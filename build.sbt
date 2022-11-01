val tapirVersion = "1.1.4"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "affirmations",
    version := "0.1.0-SNAPSHOT",
    organization := "com.protolight",
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "org.http4s" %% "http4s-blaze-server" % "0.23.12",
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.4",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "dev.zio" %% "zio-test" % "2.0.0" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.0" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.8.3" % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)
