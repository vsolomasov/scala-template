package dev.vs.adapter.input.http

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter._
import sttp.tapir.ztapir._
import zio.ZIO
import zio._

object Server {

  def run[R](endpoint: ZServerEndpoint[Any, Any])(implicit runtime: Runtime[R]): ZIO[Any, Throwable, Nothing] = {
    val attach = VertxZioServerInterpreter[R]().route(endpoint)

    ZIO.scoped(
      ZIO
        .acquireRelease(
          ZIO
            .attempt {
              val vertx = Vertx.vertx()
              val server = vertx.createHttpServer()
              val router = Router.router(vertx)
              attach(router)
              server.requestHandler(router).listen(8080)
            }
            .flatMap(_.asRIO)
        ) { server =>
          ZIO.attempt(server.close()).flatMap(_.asRIO).orDie
        } *> ZIO.never
    )
  }
}
