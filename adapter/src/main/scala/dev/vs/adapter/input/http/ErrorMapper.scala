package dev.vs.adapter.input.http

import zio.IO

object ErrorMapper:

  def defaultErrorsMappings[E <: Throwable, A](io: IO[E, A]): IO[ErrorInfo, A] = io.mapError {
    case _ => InternalServerError()
  }
