package dev.vs.adapter.input.http

import io.circe._
import io.circe.generic.semiauto._

sealed trait ErrorInfo
case class InternalServerError(error: String = "Internal server error") extends ErrorInfo

object ErrorInfo:
  given Encoder[InternalServerError] = deriveEncoder[InternalServerError]
  given Decoder[InternalServerError] = deriveDecoder[InternalServerError]
