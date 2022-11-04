package com.protolight.persistance

import zio.*
import zio.interop.catz.*
import doobie.util.transactor.Transactor
import doobie.hikari.HikariTransactor
import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import zio.interop.catz.implicits.*

case class DbConfig(url: String, user: String, password: String)

object ZioDoobieConfig {
  given zioRuntime: zio.Runtime[Any] = zio.Runtime.default
  given dispatcher: cats.effect.std.Dispatcher[zio.Task] = zio.Unsafe.unsafe {
    unsafe =>
    {
      given Unsafe = unsafe
      zioRuntime.unsafe
        .run(
          cats.effect.std
            .Dispatcher[zio.Task]
            .allocated,
        )
        .getOrThrowFiberFailure()(unsafe)
        ._1
    }
  }

  def transactor: ZIO[DbConfig & Scope, Throwable, Transactor[Task]] =
    for
      ec <- ZIO.executor.map(_.asExecutionContext)
      config <- ZIO.service[DbConfig]
      xa <- HikariTransactor
        .newHikariTransactor[Task](
          "org.postgresql.Driver",
          config.url,
          config.user,
          config.password,
          ec,
        )
        .toScopedZIO
    yield xa

  val liveTransactor: ZLayer[DbConfig, Throwable, Transactor[Task]] =
    ZLayer.scoped(transactor)
}