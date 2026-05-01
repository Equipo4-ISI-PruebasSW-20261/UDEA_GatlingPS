package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._
import scala.concurrent.duration._

// Historia de Usuario No Funcional 5: Pago de servicios con concurrencia alta
// Criterios: 200 usuarios concurrentes | ≤ 3s por transaccion | ≤ 1% errores
class BillPayTest extends Simulation {

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .disableCaching

  val payeeBody =
  s"""{
    "name": "Utility",
    "address": {
      "street": "123 Main St",
      "city": "Anytown",
      "state": "CA",
      "zipCode": "12345"
    },
    "phoneNumber": "555-555-5555",
    "accountNumber": $billPayAccountId
  }"""

  // 2 Scenario Definition
  val scn = scenario("Bill Pay")
  .exec(session =>
    session.set("amount", scala.util.Random.nextInt(500) + 1)
  )
  .exec(session =>
    session.set("billPayUrl", s"/billpay?accountId=$billPayAccountId&amount=${session("amount").as[Int]}")
  )
  .exec(
    http("Bill Payment Real")
      .post("#{billPayUrl}")
      .header("Accept", "application/json")
      .header("Content-Type", "application/json")
      .body(StringBody(payeeBody)).asJson
      .check(status.is(200))
  )

  // 3 Load Scenario
  // Historia 5: 200 usuarios concurrentes con ramp de 30 segundos
  setUp(
    scn.inject(rampUsers(200).during(30.seconds))
  ).protocols(httpConf)
   .assertions(
     // Tiempo de respuesta medio por transaccion debe ser ≤ 3 segundos
     global.responseTime.mean.lte(3000),
     // Tasa de errores funcionales debe ser ≤ 1%
     global.failedRequests.percent.lte(1)
   )
}
