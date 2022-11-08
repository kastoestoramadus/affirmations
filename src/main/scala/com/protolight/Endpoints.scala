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
  import Endpoints.*

  val helloServerEndpoint: ZServerEndpoint[Any, Any] = pingEndpoint.serverLogicSuccess(_ => ZIO.succeed("pong"))

  val getAffirmationServerEndpoint: ZServerEndpoint[Any, Any] =
    getAffirmationEndpoint.serverLogicSuccess(id => library.get(id))

  val affirmationsListingServerEndpoint: ZServerEndpoint[Any, Any] =
    affirmationsListingEndpoint.serverLogicSuccess(pagingO => library.getAll(pagingO, None))

  val createAffirmationServerEndpoint: ZServerEndpoint[Any, Any] =
    createAffirmationEndpoint.serverLogicSuccess(id => library.create(id))

  val updateAffirmationServerEndpoint: ZServerEndpoint[Any, Any] =
    updateAffirmationEndpoint.serverLogicSuccess(id => library.update(id))

  val deleteAffirmationServerEndpoint: ZServerEndpoint[Any, Any] =
    deleteAffirmationEndpoint.serverLogicSuccess(id => library.delete(id))

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] = List(
    helloServerEndpoint,
    createAffirmationServerEndpoint,
    getAffirmationServerEndpoint,
    updateAffirmationServerEndpoint,
    deleteAffirmationServerEndpoint,
    affirmationsListingServerEndpoint
  )

  // FIXME: investigate why ZServerEndpoint[AffirmationsLibrary, Any] can't be matched to _ nor ?. Forced to workaround; BUG?
  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "affirmations", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  val all: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)

object Endpoints {
  case class NotFound(what: String)

  val paging: EndpointInput[Option[Paging]] =
    query[Option[Int]]("start")
      .and(query[Option[Int]]("limit"))
      .map(input => input._1.flatMap(from => input._2.map(limit => Paging(from, limit))))(paging =>
        (paging.map(_.from), paging.map(_.limit))
      )

  val pingEndpoint: PublicEndpoint[Unit, Unit, String, Any] = endpoint.get
    .in("ping")
    .out(stringBody)

  val affirmationsListingEndpoint: PublicEndpoint[Option[Paging], Unit, List[Affirmation], Any] = endpoint.get
    .in("affirmation" / "all")
    .in(paging)
    .out(jsonBody[List[Affirmation]])

  val getAffirmationEndpoint: Endpoint[Unit, Long, Unit, Affirmation, Any] = endpoint.get
    .in("affirmation")
    .in(query[Long]("id")
      .example(9)
    )
    .out(jsonBody[Affirmation])

  val deleteAffirmationEndpoint: Endpoint[Unit, Long, Unit, Boolean, Any] = endpoint.delete
    .in("affirmation")
    .in(query[Long]("id")
      .example(738)
    )
    .out(jsonBody[Boolean])

  val createAffirmationEndpoint: PublicEndpoint[Affirmation, Unit, Affirmation, Any] = endpoint.post
    .in("affirmation")
    .in(
      jsonBody[Affirmation]
        .description("The affirmation to add.")
        .example(Affirmation(738, "Pracuję chętnie, efektywnie i z przyjemnością", "Waldemar Wosiński"))
    )
    .out(jsonBody[Affirmation])

  val updateAffirmationEndpoint: Endpoint[Unit, Affirmation, Unit, Boolean, Any] = endpoint.put
    .in("affirmation")
    .in(
      jsonBody[Affirmation]
        .description("The new state of affirmation. Id must exist already.")
        .example(Affirmation(9, "Jestem niewinny i w porządku, gdy pozwalam innym dokonywać własnych wyborów", "Waldemar Wosiński"))
    )
    .out(jsonBody[Boolean])
}
