package dev.vs.adapter.input.http

import logstage.LogZIO
import sttp.tapir.AttributeKey
import sttp.tapir.server.interceptor.RequestInterceptor
import zio.Random
import zio.Task
import zio.URLayer
import zio.ZIO
import zio.ZLayer

package object interceptor {
  type CtxInterceptor = RequestInterceptor[Task]

  val LogZioAttribute = new AttributeKey[LogZIO]("contextLog")

  private val UnknownRequestId = "unknown"

  private val RequestIdKey = "trackingId"
  private val MethodKey = "method"
  private val UriKey = "method"

  object CtxInterceptor {
    val live: URLayer[LogZIO, CtxInterceptor] = ZLayer.fromZIO {
      ZIO.service[LogZIO].map { log =>
        RequestInterceptor
          .transformServerRequest[Task] { request =>
            Random.nextUUID.map { requestId =>
              val formattedRequestId =
                requestId.toString().split("-").lastOption.getOrElse(UnknownRequestId)

              val withCtx = log.withCustomContext(
                RequestIdKey -> formattedRequestId,
                MethodKey -> request.method,
                UriKey -> request.uri
              )

              request.attribute(LogZioAttribute, withCtx)
            }
          }
      }
    }
  }
}
