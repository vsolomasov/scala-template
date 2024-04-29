package dev.vs.adapter.input.http.system

import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.URLayer
import zio.ZLayer

class SystemEndpoints(
  livess: LivenessEndpoint
) {

  val endpoints: List[ZServerEndpoint[Any, Any]] = {
    val api: List[ZServerEndpoint[Any, Any]] = livess.endpoint :: Nil
    val docs = SwaggerInterpreter().fromServerEndpoints[Task](api, "template-tapir-zio", "dev")
    api ::: docs
  }
}

object SystemEndpoints:
  val live: URLayer[LivenessEndpoint, SystemEndpoints] =
    ZLayer.fromFunction(SystemEndpoints(_))
