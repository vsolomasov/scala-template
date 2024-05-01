package dev.vs.adapter.input.http.system.readiness

import dev.vs.adapter.input.http.BaseEndpoints
import dev.vs.adapter.input.http.Endpoint
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio.URLayer
import zio.ZIO
import zio.ZLayer

class ReadinessEndpoint(base: BaseEndpoints) extends Endpoint:
  val protocol = base.publicEndpoint.get
    .in("system" / "readiness")
    .out(jsonBody[ReadinessResponse])

  val endpoint: ZServerEndpoint[Any, Any] =
    protocol.zServerLogic(_ => ZIO.succeed(ReadinessResponse("Ready")))

object ReadinessEndpoint:
  val live: URLayer[BaseEndpoints, ReadinessEndpoint] =
    ZLayer.fromFunction(new ReadinessEndpoint(_))
