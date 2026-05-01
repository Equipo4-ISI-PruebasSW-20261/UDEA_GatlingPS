package parabank

object Data {
  val url           = "https://parabank.parasoft.com/parabank/services/bank"
  val username      = "john"
  val password      = "demo"
  val fromAccountId = "12345"
  val toAccountId   = "12456"
  val amount        = "1"

  // Historia 3 - Consulta de estados de cuenta
  val accountId = "12345"

  // Historia 4 - Solicitud de prestamo
  val customerId  = "12212"
  val loanAmount  = "1000"
  val downPayment = "100"

  // Historia 5 - Pago de servicios
  val billPayAccountId = "12345"
  val billPayAmount    = "1"
}
