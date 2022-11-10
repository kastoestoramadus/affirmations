package com.protolight

import com.protolight.AffirmationsLibrary.{Affirmation, OperationSuccessful, Paging}
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
import sttp.model.StatusCode

object ApiEndpoints {
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

  val getAffirmationEndpoint: Endpoint[Unit, Long, GetAffirmation.ErrorResponse, Affirmation, Any] = endpoint.get
    .in("affirmation")
    .in(
      query[Long]("id")
        .example(9)
    )
    .out(jsonBody[Affirmation])
    .errorOut(
      oneOf[GetAffirmation.ErrorResponse](
        oneOfVariant(StatusCode.NotFound, jsonBody[GetAffirmation.NotFound])
      )
    )

  object GetAffirmation {
    trait ErrorResponse extends Throwable
    case class NotFound(id: Long) extends ErrorResponse
  }

  val deleteAffirmationEndpoint: Endpoint[Unit, Long, DeleteAffirmation.ErrorResponse, OperationSuccessful.type, Any] = endpoint.delete
    .in("affirmation")
    .in(
      query[Long]("id")
        .example(738)
    )
    .out(jsonBody[OperationSuccessful.type])
    .errorOut(
      oneOf[DeleteAffirmation.ErrorResponse](
        oneOfVariant(StatusCode.NotFound, jsonBody[DeleteAffirmation.NotFound])
      )
    )

  object DeleteAffirmation {
    trait ErrorResponse extends Throwable
    case class NotFound(id: Long) extends ErrorResponse
  }

  val createAffirmationEndpoint: PublicEndpoint[Affirmation, CreateAffirmation.ErrorResponse, Affirmation, Any] = endpoint.post
    .in("affirmation")
    .in(
      jsonBody[Affirmation]
        .description("The affirmation to add.")
        .example(Affirmation(738, "Pracuję chętnie, efektywnie i z przyjemnością", "Waldemar Wosiński"))
    )
    .out(jsonBody[Affirmation])
    .errorOut(
      oneOf[CreateAffirmation.ErrorResponse](
        oneOfVariant(StatusCode.BadRequest, jsonBody[CreateAffirmation.IdAlreadyTaken])
      )
    )

  object CreateAffirmation {
    trait ErrorResponse extends Throwable
    case class IdAlreadyTaken(id: Long) extends ErrorResponse
  }

  val updateAffirmationEndpoint: Endpoint[Unit, Affirmation, UpdateAffirmation.ErrorResponse, OperationSuccessful.type, Any] = endpoint.put
    .in("affirmation")
    .in(
      jsonBody[Affirmation]
        .description("The new state of affirmation. Id must exist already.")
        .example(Affirmation(9, "Jestem niewinny i w porządku, gdy pozwalam innym dokonywać własnych wyborów", "Waldemar Wosiński"))
    )
    .out(jsonBody[OperationSuccessful.type])
    .errorOut(
      oneOf[UpdateAffirmation.ErrorResponse](
        oneOfVariant(StatusCode.NotFound, jsonBody[UpdateAffirmation.NotFound])
      )
    )

  object UpdateAffirmation {
    trait ErrorResponse extends Throwable
    case class NotFound(id: Long) extends ErrorResponse
  }
}
