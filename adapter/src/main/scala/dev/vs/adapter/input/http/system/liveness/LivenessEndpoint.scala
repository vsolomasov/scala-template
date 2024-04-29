package dev.vs.adapter.input.http.system.liveness

import dev.vs.adapter.input.http.BaseEndpoints
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio.URLayer
import zio.ZIO
import zio.ZLayer

class LivenessEndpoint(base: BaseEndpoints):
  val protocol = base.publicEndpoint.get
    .in("system" / "liveness")
    .out(jsonBody[LivenessResponse])

  val endpoint: ZServerEndpoint[Any, Any] = protocol.zServerLogic(_ => ZIO.succeed(LivenessResponse("ok")))

object LivenessEndpoint:
  val live: URLayer[BaseEndpoints, LivenessEndpoint] =
    ZLayer.fromFunction(new LivenessEndpoint(_))
