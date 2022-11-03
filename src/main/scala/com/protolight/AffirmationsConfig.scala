package com.protolight
import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class AffirmationsConfig(port: Int)

object AffirmationsConfig {
  val layer: ZLayer[Any, ReadError[String], AffirmationsConfig] =
    ZLayer {
      read {
        descriptor[AffirmationsConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("AffirmationsConfig"))
        )
      }
    }
}