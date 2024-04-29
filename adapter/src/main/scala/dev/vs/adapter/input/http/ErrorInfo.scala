package dev.vs.adapter.input.http

import io.circe._
import io.circe.generic.semiauto._

sealed trait ErrorInfo
case class InternalServerError(error: String = "Internal server error") extends ErrorInfo

object ErrorInfo:
  given internalServerErrorEncoder: Encoder[InternalServerError] = deriveEncoder[InternalServerError]
  given internalServerErrorDecoder: Decoder[InternalServerError] = deriveDecoder[InternalServerError]
