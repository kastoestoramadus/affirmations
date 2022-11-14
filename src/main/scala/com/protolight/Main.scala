package com.protolight

import com.protolight.ServerEndpoints.DepsWorkaround
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
    def app: HttpApp[AffirmationsLibrary, Throwable] = {
      val serverOptions =
        ZioHttpServerOptions.customiseInterceptors
          .serverLog(
            DefaultServerLog[DepsWorkaround](
              doLogWhenReceived = msg => ZIO.service[AffirmationsLibrary] *> ZIO.succeed(log.debug(msg)),
              doLogWhenHandled =
                (msg, error) => ZIO.service[AffirmationsLibrary] *> ZIO.succeed(error.fold(log.debug(msg))(err => log.debug(msg, err))),
              doLogAllDecodeFailures =
                (msg, error) => ZIO.service[AffirmationsLibrary] *> ZIO.succeed(error.fold(log.debug(msg))(err => log.debug(msg, err))),
              doLogExceptions = (msg: String, ex: Throwable) => ZIO.service[AffirmationsLibrary] *> ZIO.succeed(log.debug(msg, ex)),
              noLog = ZIO.service[AffirmationsLibrary] *> ZIO.unit
            )
          )
          .metricsInterceptor(ServerEndpoints.prometheusMetrics.metricsInterceptor())
          .options

      ZioHttpInterpreter(serverOptions).toHttp(ServerEndpoints.all)
    }

    (for
      config <- ZIO.service[AffirmationsConfig]
      serverStart <- Server.start(port = config.api.port, http = app)
      _ <- Console.printLine(s"Go to http://localhost:${config.api.port}/docs to open SwaggerUI.")
      _ <- Console.readLine
    yield serverStart)
      .provideSomeLayer(EventLoopGroup.auto(0) ++ ServerChannelFactory.auto ++ Scope.default)
      .provide(
        AffirmationsConfig.layer,
        ZioDoobieConfig.liveTransactor,
        PersistentLibrary.live
      )
