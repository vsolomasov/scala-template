package dev.vs.adapter.input.http.system

import dev.vs.adapter.input.http.Endpoints
import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import dev.vs.adapter.input.http.system.readiness.ReadinessEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.URLayer
import zio.ZLayer

class SystemEndpoints(
  livess: LivenessEndpoint,
  readiness: ReadinessEndpoint
) extends Endpoints:

  val endpoints: List[ZServerEndpoint[Any, Any]] = {
    val api: List[ZServerEndpoint[Any, Any]] = (livess :: readiness :: Nil).map(_.endpoint)
    val docs = SwaggerInterpreter().fromServerEndpoints[Task](api, "template-tapir-zio", "dev")
    api ::: docs
  }

object SystemEndpoints:
  val live: URLayer[LivenessEndpoint & ReadinessEndpoint, SystemEndpoints] =
    ZLayer.fromFunction(SystemEndpoints(_, _))
