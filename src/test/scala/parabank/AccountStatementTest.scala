package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._
import scala.concurrent.duration._

// Historia de Usuario No Funcional 3: Carga simultanea de estados de cuenta
// Criterios: ≤ 3s con 200 usuarios simultaneos | tasa de error ≤ 1%
class AccountStatementTest extends Simulation {

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")

  // 2 Scenario Definition
  val scn = scenario("Account Statement - Carga Simultanea")
    .exec(
      http("Get Account Transactions")
        .get(s"/accounts/$accountId/transactions")
        .check(status.is(200))
    )

  // 3 Load Scenario
  // Historia 3: 200 usuarios simultaneos con ramp de 30 segundos
  setUp(
    scn.inject(rampUsers(200).during(30.seconds))
  ).protocols(httpConf)
   .assertions(
     // Tiempo de respuesta medio ≤ 3 segundos con 200 usuarios
     global.responseTime.mean.lte(3000),
     // Tasa de error no debe superar el 1%
     global.failedRequests.percent.lte(1)
   )
}