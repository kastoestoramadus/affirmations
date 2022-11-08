package com.protolight

import com.protolight.persistance.{PersistentLibrary, ZioDoobieConfig}
import org.slf4j.LoggerFactory
import sttp.tapir.server.interceptor.log.DefaultServerLog
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zhttp.http.HttpApp
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.*

object Main extends ZIOAppDefault:
  val log = LoggerFactory.getLogger(ZioHttpInterpreter.getClass.getName)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =

    def app(library: AffirmationsLibrary): HttpApp[Any, Throwable] = {
      val endpoints = Endpoints(library)
      val serverOptions: ZioHttpServerOptions[Any] =
        ZioHttpServerOptions.customiseInterceptors
          .serverLog(
            DefaultServerLog[Task](
              doLogWhenReceived = msg => ZIO.succeed(log.debug(msg)),
              doLogWhenHandled = (msg, error) => ZIO.succeed(error.fold(log.debug(msg))(err => log.debug(msg, err))),
              doLogAllDecodeFailures = (msg, error) => ZIO.succeed(error.fold(log.debug(msg))(err => log.debug(msg, err))),
              doLogExceptions = (msg: String, ex: Throwable) => ZIO.succeed(log.debug(msg, ex)),
              noLog = ZIO.unit
            )
          )
          .metricsInterceptor(endpoints.prometheusMetrics.metricsInterceptor())
          .options

      ZioHttpInterpreter(serverOptions).toHttp(endpoints.all)
    }

    (for
      config <- ZIO.service[AffirmationsConfig]
      library <- ZIO.service[AffirmationsLibrary]
      serverStart <- Server.start(port = config.api.port, http = app(library))
      _ <- Console.printLine(s"Go to http://localhost:${config.api.port}/docs to open SwaggerUI.")
    yield serverStart)
      .provideSomeLayer(EventLoopGroup.auto(0) ++ ServerChannelFactory.auto ++ Scope.default)
      .provide(
        AffirmationsConfig.layer,
        ZioDoobieConfig.liveTransactor,
        PersistentLibrary.live
      )
