package dev.vs.adapter.input.http.interceptor

import logstage.LogZIO
import sttp.tapir.AttributeKey
import sttp.tapir.server.interceptor.EndpointInterceptor
import sttp.tapir.server.interceptor.RequestHandler
import sttp.tapir.server.interceptor.RequestInterceptor
import sttp.tapir.server.interceptor.Responder
import zio.Random
import zio.Task
import zio.URLayer
import zio.ZIO
import zio.ZLayer

package object ctx {

  class CtxInterceptor(delegate: RequestInterceptor[Task]) extends RequestInterceptor[Task]:

    override def apply[R, B](
      responder: Responder[Task, B],
      requestHandler: EndpointInterceptor[Task] => RequestHandler[Task, R, B]
    ): RequestHandler[Task, R, B] = delegate.apply[R, B](responder, requestHandler)

  val LogZioAttribute = new AttributeKey[LogZIO]("contextLog")

  private val UnknownRequestId = "unknown"

  private val RequestIdKey = "trackingId"
  private val MethodKey = "method"
  private val UriKey = "method"

  object CtxInterceptor {
    val live: URLayer[LogZIO, CtxInterceptor] = ZLayer.fromZIO {
      ZIO.service[LogZIO].map { log =>
        val delegate = RequestInterceptor
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
        new CtxInterceptor(delegate)
      }
    }
  }
}
