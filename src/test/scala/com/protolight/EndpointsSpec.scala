package com.protolight

import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}
import AffirmationsLibrary.*
import com.protolight.persistance.InMemoryLibrary
import io.circe.generic.auto.*
import sttp.client3.circe.*
import sttp.tapir.ztapir.RIOMonadError

object EndpointsSpec extends ZIOSpecDefault:
  val library = InMemoryLibrary

  def spec = suite("Endpoints spec")(
    test("return pong message") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(ServerEndpoints.helloServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .get(uri"http://test.com/ping")
        .send(backendStub)

      // then
      assertZIO(response.map(_.body))(isRight(equalTo("pong")))
    },
    test("list available affirmations") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpoint(ServerEndpoints.affirmationsListingServerEndpoint)
        .thenRunLogic()
        .backend()

      // when
      val response = basicRequest
        .get(uri"http://test.com/affirmation/all")
        .response(asJson[List[Affirmation]])
        .send(backendStub)

      // then
      assertZIO(response.map(_.body))(isRight(equalTo(library.affirmations.toList)))
    }
  )
