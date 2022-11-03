package com.protolight

import org.slf4j.LoggerFactory
import sttp.tapir.server.interceptor.log.DefaultServerLog
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zhttp.http.HttpApp
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.{Console, Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault:
  val log = LoggerFactory.getLogger(ZioHttpInterpreter.getClass.getName)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =

    val app: HttpApp[Any, Throwable] = {
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
          .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
          .options

      ZioHttpInterpreter(serverOptions).toHttp(Endpoints.all)
    }

    ZIO.service[AffirmationsConfig].flatMap{ config =>
      (for
        serverStart <- Server.start(port = config.port, http = app)
        _ <- Console.printLine(s"Go to http://localhost:${config.port}/docs to open SwaggerUI.")
      yield serverStart)
        .provideSomeLayer(EventLoopGroup.auto(0) ++ ServerChannelFactory.auto ++ Scope.default)
    }.provide(
      AffirmationsConfig.layer
    )

