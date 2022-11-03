package com.protolight

import sttp.tapir.*

import Library.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.ZIO

object Endpoints:
  val pingEndpoint: PublicEndpoint[Unit, Unit, String, Any] = endpoint.get
    .in("ping")
    .out(stringBody)
  val helloServerEndpoint: ZServerEndpoint[Any, Any] = pingEndpoint.serverLogicSuccess(_ => ZIO.succeed("pong"))

  case class Paging(from: Int, limit: Int)

  val paging: EndpointInput[Option[Paging]] =
    query[Option[Int]]("start").and(query[Option[Int]]("limit"))
      .map(input =>
        input._1.flatMap(from => input._2.map(limit => Paging(from, limit)))
      )(paging => (paging.map(_.from), paging.map(_.limit)))

  val booksListing: PublicEndpoint[Option[Paging], Unit, List[Affirmation], Any] = endpoint.get
    .in("affirmations" / "list" / "all")
    .in(paging)
    .out(jsonBody[List[Affirmation]])
  val booksListingServerEndpoint: ZServerEndpoint[Any, Any] =
    booksListing.serverLogicSuccess(_ => ZIO.succeed(Library.affirmations))

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] = List(helloServerEndpoint, booksListingServerEndpoint)

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "affirmations", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  val all: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)

object Library:
  type Author = String
  case class Affirmation(content: String, author: Author)

  val affirmations = List(
    Affirmation("Akceptuję cuda i wszelkie pozytywne zmiany w całym swoim życiu", "Johann Wolfgang von Goethe"),
    Affirmation("Akceptuję doskonałe zdrowie i wygląd swojego ciała", "Eliza Orzeszkowa"),
    Affirmation("Akceptuję i doceniam wysoki potencjał mojego ciała", "Donald Knuth"),
    Affirmation("Akceptuję i szanuję swoje seksualne ciało", "Boleslaw Prus")
  )
