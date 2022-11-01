package com.protolight

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.interop.catz.*
import zio.{Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault}
import scala.io.StdIn

object Main extends ZIOAppDefault:

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =

    val serverOptions: Http4sServerOptions[Task] =
      Http4sServerOptions
        .customiseInterceptors[Task]
        .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
        .options
    val routes = ZHttp4sServerInterpreter(serverOptions).from(Endpoints.all).toRoutes

    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)

    ZIO.executor.flatMap { executor =>
      BlazeServerBuilder[Task]
        .withExecutionContext(executor.asExecutionContext)
        .bindHttp(port, "localhost")
        .withHttpApp(Router("/" -> routes).orNotFound)
        .resource
        .use { server =>
          ZIO.succeedBlocking {
            println(s"Go to http://localhost:${server.address.getPort}/docs to open SwaggerUI.Press ENTER key to exit.")
            StdIn.readLine()
          }
        }
        .unit
    }
