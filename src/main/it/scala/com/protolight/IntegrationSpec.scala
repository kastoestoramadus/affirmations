package com.protolight

import sttp.client3.testing.SttpBackendStub
import sttp.client3.{HttpClientSyncBackend, UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}
import AffirmationsLibrary.*
import com.protolight.persistance.InMemoryLibrary
import io.circe.generic.auto.*
import sttp.client3.circe.*
import sttp.tapir.ztapir.RIOMonadError
import zio.ZIO

object IntegrationSpec extends ZIOSpecDefault:
  val backend = HttpClientSyncBackend()

  def spec = suite("Integration spec")(
    test("list available affirmations") {
      // when
      val response = basicRequest
        .get(uri"http://localhost:8080/affirmation/all?start=0&limit=4")
        .response(asJson[List[Affirmation]])
        .send(backend)

      // then
      assertZIO(ZIO.succeed(response.body))(
        isRight(
          equalTo(
            List(
              Affirmation(0, "Akceptuję cuda i wszelkie pozytywne zmiany w całym swoim życiu", "afirmacje.pl"),
              Affirmation(1, "Akceptuję doskonałe zdrowie i wygląd swojego ciała", "afirmacje.pl"),
              Affirmation(2, "Akceptuję i doceniam wysoki potencjał mojego ciała", "afirmacje.pl"),
              Affirmation(3, "Akceptuję i szanuję swoje seksualne ciało", "afirmacje.pl")
            )
          )
        )
      )
    }
  )
