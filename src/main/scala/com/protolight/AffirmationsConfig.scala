package com.protolight
import com.protolight.persistance.DbConfig
import zio.*
import zio.config.*
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class AffirmationsConfig(api: ApiConfig, db: DbConfig)
case class ApiConfig(port: Int)


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