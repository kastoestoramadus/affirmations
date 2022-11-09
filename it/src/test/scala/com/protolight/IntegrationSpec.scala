package com.protolight

import io.circe.generic.auto.*
import sttp.client3.circe.*
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{HttpClientSyncBackend, UriContext, basicRequest}
import zio.ZIO
import zio.test.Assertion.*
import zio.test.{test, *}

object IntegrationSpec extends ZIOSpecDefault:
  val backend = HttpClientSyncBackend()

  case class Affirmation(id: Long, content: String, author: String)

  def spec = suite("Integration spec")(
    test("list available affirmations") {
      // when
      val response = basicRequest
        .get(uri"http://localhost:8080/affirmation/all?start=111&limit=3")
        .response(asJson[List[Affirmation]])
        .send(backend)

      // then
      assertZIO(ZIO.succeed(response.body))(
        isRight(
          equalTo(
            List(
              Affirmation(112, "Dziękuję sobie za cierpliwość i konsekwencję w dążeniu do swoich celów","afirmacje.pl"),
              Affirmation(113, "Dziękuję za bogactwo, które mnie otacza","afirmacje.pl"),
              Affirmation(114, "Dziękuję za doskonałe zdrowie i samopoczucie","afirmacje.pl"),
            )
          )
        )
      )
    }
  )
