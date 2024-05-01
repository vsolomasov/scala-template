package dev.vs.adapter.output

import dev.vs.adapter.input.http.interceptor.LogZioAttribute
import dev.vs.adapter.output.ServerLogZioImpl.ServerLogZIO
import logstage.LogIO
import logstage.LogZIO
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.interceptor.DecodeFailureContext
import sttp.tapir.server.interceptor.DecodeSuccessContext
import sttp.tapir.server.interceptor.SecurityFailureContext
import sttp.tapir.server.interceptor.log.ExceptionContext
import sttp.tapir.server.interceptor.log.ServerLog
import sttp.tapir.server.model.ServerResponse
import zio.Task
import zio.URLayer
import zio.ZIO
import zio.ZLayer

import java.util.UUID

class ServerLogZioImpl(log: LogZIO) extends ServerLogZIO:

  override type TOKEN = UUID

  override def requestToken: TOKEN = UUID.randomUUID()

  override def exception(ctx: ExceptionContext[?, ?], ex: Throwable, token: TOKEN): Task[Unit] =
    log.info("Exception")

  override def requestReceived(request: ServerRequest, token: TOKEN): Task[Unit] = {
    val contextLog = request.attribute(LogZioAttribute).getOrElse(log)
    contextLog.info("Request received")
  }

  override def requestHandled(
    ctx: DecodeSuccessContext[Task, ?, ?, ?],
    response: ServerResponse[?],
    token: TOKEN
  ): Task[Unit] = {
    val contextLog =
      ctx.request
        .attribute(LogZioAttribute)
        .map(_.withCustomContext("code" -> response.showShort))
        .getOrElse(log)
    contextLog.info("Request handled")
  }

  override def decodeFailureHandled(
    ctx: DecodeFailureContext,
    response: ServerResponse[?],
    token: TOKEN
  ): Task[Unit] = {
    val contextLog =
      ctx.request
        .attribute(LogZioAttribute)
        .map(_.withCustomContext("code" -> response.showShort))
        .getOrElse(log)
    contextLog.info("Decode failure handled")
  }

  override def securityFailureHandled(
    ctx: SecurityFailureContext[Task, ?],
    response: ServerResponse[?],
    token: TOKEN
  ): Task[Unit] =
    log.info("Security failure handled")

  override def decodeFailureNotHandled(ctx: DecodeFailureContext, token: TOKEN): Task[Unit] =
    log.info("Decode failure not handled")

object ServerLogZioImpl:

  type ServerLogZIO = ServerLog[Task]

  val live: URLayer[LogZIO, ServerLogZIO] =
    ZLayer.fromFunction(new ServerLogZioImpl(_))
