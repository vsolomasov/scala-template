package dev.vs.adapter.input.http.system.readiness

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto._

final case class ReadinessResponse(status: String)

object ReadinessResponse:
  given Encoder[ReadinessResponse] = deriveEncoder[ReadinessResponse]
  given Decoder[ReadinessResponse] = deriveDecoder[ReadinessResponse]
