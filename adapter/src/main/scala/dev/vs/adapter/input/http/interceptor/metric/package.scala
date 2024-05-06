package dev.vs.adapter.input.http.interceptor

import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor
import sttp.tapir.server.metrics.zio.ZioMetrics
import zio.Task
import zio.ULayer
import zio.ZLayer

package object metric {
  type ServerMetricInterceptor = MetricsRequestInterceptor[Task]

  object ServerMetricInterceptor:
    val live: ULayer[ServerMetricInterceptor] = ZLayer.succeed {
      ZioMetrics.default[Task]().metricsInterceptor()
    }
}
