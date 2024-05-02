package dev.vs.adapter.input.http.system.liveness

import dev.vs.adapter.input.http.BaseEndpoints
import dev.vs.adapter.input.http.Endpoint
import dev.vs.adapter.input.http.ErrorMapper.defaultErrorsMappings
import io.micrometer.prometheus.PrometheusMeterRegistry
import sttp.tapir.ztapir._
import zio.URLayer
import zio.ZIO
import zio.ZLayer

import scala.util.chaining._

class MetricsEndpoint(base: BaseEndpoints, meterRegistry: PrometheusMeterRegistry) extends Endpoint:
  val protocol = base.publicEndpoint.get
    .in("system" / "metrics")
    .out(stringBody)

  val endpoint: ZServerEndpoint[Any, Any] = protocol.zServerLogic { _ =>
    ZIO
      .attempt(meterRegistry.scrape())
      .pipe(defaultErrorsMappings)
  }

object MetricsEndpoint:
  val live: URLayer[BaseEndpoints & PrometheusMeterRegistry, MetricsEndpoint] =
    ZLayer.fromFunction(new MetricsEndpoint(_, _))
