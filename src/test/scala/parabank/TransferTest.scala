package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._
import scala.concurrent.duration._

// Historia de Usuario No Funcional 2: Transferencias simultaneas
// Criterios: ≥ 150 transacciones/segundo | sin fallos | feeder CSV
class TransferTest extends Simulation {

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")

  // Feeder CSV para datos de transferencias (Historia 2)
  val transferFeeder = csv("transfers.csv").circular

  // 2 Scenario Definition
  val scn = scenario("Transfer Test - Escalabilidad")
    .feed(transferFeeder)
    .exec(
      http("Transfer Request")
        .post("/transfer")
        .queryParam("fromAccountId", session => session("fromAccountId").as[String])
        .queryParam("toAccountId", session => session("toAccountId").as[String])
        .queryParam("amount", session => session("amount").as[String])
        .check(status.is(200))
    )

  // 3 Load Scenario
  // Historia 2: rampa hasta 150 tps, luego sostenido por 60s para prueba de estres
  setUp(
    scn.inject(
      rampUsersPerSec(10).to(150).during(30.seconds),
      constantUsersPerSec(150).during(60.seconds)
    )
  ).protocols(httpConf)
   .assertions(
     // No deben perderse más del 1% de transacciones
     global.failedRequests.percent.lte(1),
     global.successfulRequests.percent.gte(99)
   )
}
