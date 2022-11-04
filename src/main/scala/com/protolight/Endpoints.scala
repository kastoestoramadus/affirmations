package com.protolight

import com.protolight.AffirmationsLibrary.{Affirmation, Paging}
import sttp.tapir.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.ZIO

class Endpoints(library: AffirmationsLibrary):
  val pingEndpoint: PublicEndpoint[Unit, Unit, String, Any] = endpoint.get
    .in("ping")
    .out(stringBody)
  val helloServerEndpoint: ZServerEndpoint[Any, Any] = pingEndpoint.serverLogicSuccess(_ => ZIO.succeed("pong"))

  val paging: EndpointInput[Option[Paging]] =
    query[Option[Int]]("start").and(query[Option[Int]]("limit"))
      .map(input =>
        input._1.flatMap(from => input._2.map(limit => Paging(from, limit)))
      )(paging => (paging.map(_.from), paging.map(_.limit)))

  val affirmationsListing: PublicEndpoint[Option[Paging], Unit, List[Affirmation], Any] = endpoint.get
    .in("affirmations" / "list" / "all")
    .in(paging)
    .out(jsonBody[List[Affirmation]])

  val affirmationsListingServerEndpoint: ZServerEndpoint[Any, Any] =
    affirmationsListing.serverLogicSuccess(pagingO => library.getAll(pagingO, None))

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] = List(helloServerEndpoint, affirmationsListingServerEndpoint)

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "affirmations", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  val all: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)


