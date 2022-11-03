enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
val tapirVersion = "1.1.4"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "affirmations",
    version := "0.1.0-SNAPSHOT",
    organization := "com.protolight",
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "org.http4s" %% "http4s-blaze-server" % "0.23.12",
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % "1.3.4", // don't upgrade to 1.4.x, docker won't start nor show logs
      "dev.zio" %% "zio-config" % "3.0.2",
      "dev.zio" %% "zio-config-typesafe" % "3.0.2",
      "dev.zio" %% "zio-config-magnolia" % "3.0.2",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "dev.zio" %% "zio-test" % "2.0.3" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.3" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.8.3" % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    dockerExposedPorts ++= Seq(8080)
  )
)
