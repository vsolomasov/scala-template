package dev.vs.adapter.input.http.system.liveness

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto._

final case class LivenessResponse(status: String)

object LivenessResponse:
  given Encoder[LivenessResponse] = deriveEncoder[LivenessResponse]
  given Decoder[LivenessResponse] = deriveDecoder[LivenessResponse]
