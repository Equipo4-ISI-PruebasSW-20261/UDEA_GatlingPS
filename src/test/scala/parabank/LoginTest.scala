package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._
import scala.concurrent.duration._

// Historia de Usuario No Funcional 1: Tiempo de respuesta en login
// Criterios: ≤ 2s con 100 usuarios | ≤ 5s con 200 usuarios (carga pico)
class LoginTest extends Simulation {

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")

  // 2 Scenario Definition
  // Carga normal: 100 usuarios concurrentes
  val scnNormal = scenario("Login Test - Carga Normal")
    .exec(
      http("Login Carga Normal")
        .get(s"/login/$username/$password")
        .check(status.is(200))
    )

  // Carga pico: 200 usuarios concurrentes
  val scnPico = scenario("Login Test - Carga Pico")
    .exec(
      http("Login Carga Pico")
        .get(s"/login/$username/$password")
        .check(status.is(200))
    )

  // 3 Load Scenario
  // Ejecutar secuencialmente: primero carga normal, luego carga pico
  setUp(
    scnNormal.inject(
      constantConcurrentUsers(100).during(30.seconds)
    ),
    scnPico.inject(
      constantConcurrentUsers(0).during(30.seconds),
      constantConcurrentUsers(200).during(30.seconds)
    )
  ).protocols(httpConf)
   .assertions(
     // Historia 1: ≤ 2 segundos con 100 usuarios concurrentes (carga normal)
     details("Login Carga Normal").responseTime.max.lte(2000),
     // Historia 1: ≤ 5 segundos con 200 usuarios concurrentes (carga pico)
     details("Login Carga Pico").responseTime.max.lte(5000),
     // Tasa de error global ≤ 1%
     global.failedRequests.percent.lte(1)
   )
}
