package dev.vs.adapter.input.http

import dev.vs.adapter.input.http.BaseEndpoints.defaultErrorOutputs
import sttp.model.StatusCode
import sttp.tapir.EndpointOutput
import sttp.tapir.PublicEndpoint
import sttp.tapir.Validator
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import zio.ULayer
import zio.ZLayer

class BaseEndpoints():
  val publicEndpoint: PublicEndpoint[Unit, ErrorInfo, Unit, Any] = endpoint
    .errorOut(defaultErrorOutputs)

object BaseEndpoints:
  val live: ULayer[BaseEndpoints] = ZLayer.succeed(new BaseEndpoints())

  val defaultErrorOutputs: EndpointOutput.OneOf[ErrorInfo, ErrorInfo] = oneOf[ErrorInfo](
    oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[InternalServerError]))
  )
