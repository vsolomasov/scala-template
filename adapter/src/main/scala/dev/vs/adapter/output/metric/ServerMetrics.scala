package dev.vs.adapter.output.metric

import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor
import sttp.tapir.server.metrics.zio.ZioMetrics
import zio.Task
import zio.ULayer
import zio.ZLayer

object ServerMetrics {

  type MetricsInterceptor = MetricsRequestInterceptor[Task]

  val live: ULayer[MetricsInterceptor] = ZLayer.succeed {
    ZioMetrics.default[Task]().metricsInterceptor()
  }
}
