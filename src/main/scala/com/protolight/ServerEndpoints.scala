package com.protolight

import com.protolight.AffirmationsLibrary.{Affirmation, Paging}
import sttp.tapir.*
import io.circe.generic.auto.*
import sttp.tapir.ztapir.RichZServerEndpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.ZIO
import com.protolight.ApiEndpoints.*

object ServerEndpoints:
  import AffirmationsLibrary.*

  val helloServerEndpoint: ZServerEndpoint[Any, Any] = pingEndpoint.serverLogicSuccess(_ => ZIO.succeed("pong"))

  val getAffirmationServerEndpoint: ZServerEndpoint[AffirmationsLibrary, Any] =
    getAffirmationEndpoint.serverLogic(id => get(id).map(_.left.map(old => GetAffirmation.NotFound(old.id))))

  val affirmationsListingServerEndpoint: ZServerEndpoint[AffirmationsLibrary, Any] =
    affirmationsListingEndpoint.serverLogicSuccess(pagingO => getAll(pagingO, None))

  val createAffirmationServerEndpoint: ZServerEndpoint[AffirmationsLibrary, Any] =
    createAffirmationEndpoint.serverLogic(id => create(id).map(_.left.map(old => CreateAffirmation.IdAlreadyTaken(old.id))))

  val updateAffirmationServerEndpoint: ZServerEndpoint[AffirmationsLibrary, Any] =
    updateAffirmationEndpoint.serverLogic(id => update(id).map(_.left.map(old => UpdateAffirmation.NotFound(old.id))))

  val deleteAffirmationServerEndpoint: ZServerEndpoint[AffirmationsLibrary, Any] =
    deleteAffirmationEndpoint.serverLogic(id => delete(id).map(_.left.map(old => DeleteAffirmation.NotFound(old.id))))

  val apiEndpoints: List[ZServerEndpoint[AffirmationsLibrary, Any]] = List(
    helloServerEndpoint.widen[AffirmationsLibrary],
    createAffirmationServerEndpoint,
    getAffirmationServerEndpoint,
    updateAffirmationServerEndpoint,
    deleteAffirmationServerEndpoint,
    affirmationsListingServerEndpoint
  )

  val docEndpoints: List[ZServerEndpoint[AffirmationsLibrary, Any]] = SwaggerInterpreter()
    .fromServerEndpoints(apiEndpoints, "affirmations", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[AffirmationsLibrary, Any] = prometheusMetrics.metricsEndpoint.widen[AffirmationsLibrary]

  val all: List[ZServerEndpoint[AffirmationsLibrary, Any]] =
    apiEndpoints ++ docEndpoints ++ List(metricsEndpoint.widen[AffirmationsLibrary])
