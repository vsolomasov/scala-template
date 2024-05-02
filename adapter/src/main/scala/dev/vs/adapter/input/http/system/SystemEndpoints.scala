package dev.vs.adapter.input.http.system

import dev.vs.adapter.input.config.AppInfo
import dev.vs.adapter.input.http.Endpoints
import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import dev.vs.adapter.input.http.system.liveness.MetricsEndpoint
import dev.vs.adapter.input.http.system.readiness.ReadinessEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.URLayer
import zio.ZLayer

class SystemEndpoints(
  liveness: LivenessEndpoint,
  readiness: ReadinessEndpoint,
  metrics: MetricsEndpoint,
  info: AppInfo
) extends Endpoints:

  val endpoints: List[ZServerEndpoint[Any, Any]] = {
    val api: List[ZServerEndpoint[Any, Any]] =
      (liveness :: readiness :: metrics :: Nil).map(_.endpoint)
    val docs = SwaggerInterpreter().fromServerEndpoints[Task](api, info.name, info.version)
    api ::: docs
  }

object SystemEndpoints:
  type SysEnv = LivenessEndpoint & ReadinessEndpoint & MetricsEndpoint & AppInfo

  val live: URLayer[SysEnv, SystemEndpoints] =
    ZLayer.fromFunction(SystemEndpoints(_, _, _, _))
