package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._

class LoginTest extends Simulation{

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition
  val scn100 = scenario("Login").
    exec(http("login")
      .get(s"/login/$username/$password")
       //Recibir información de la cuenta
      .check(status.is(200))
    )

  val scn200 = scenario("Login").
    exec(http("login")
      .get(s"/login/$username/$password")
       //Recibir información de la cuenta
      .check(status.is(200))
    )

  val cargaNormal = scn100.inject(
    rampUsers(100).during(10.seconds)
  )

  val cargaPico = scn200.inject(
    rampUsers(200).during(20.seconds)
  )

  // 3 Load Scenario
  setUp(
    cargaNormal,
    cargaPico
  ).protocols(httpConf)
   .assertions(
      // Tiempo medio ≤ 2s bajo carga normal
      global.responseTime.mean.lte(2000),
      // Tiempo máx ≤ 5s bajo carga pico
      global.responseTime.max.lte(5000),
      // Menos del 1% de errores
      global.failedRequests.percent.lte(1)
   )
}
