package dev.vs.adapter.input.http

import dev.vs.adapter.input.config.AppConfig.ServerConfig
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter._
import sttp.tapir.ztapir._
import zio.ZIO
import zio._

object Server {

  def run[R](
    endpoints: List[ZServerEndpoint[Any, Any]],
    server: ServerConfig
  )(implicit
    runtime: Runtime[R]
  ): ZIO[Any, Throwable, Nothing] = {
    for {
      runtime <- ZIO.runtime
      vertx <- ZIO.succeed(Vertx.vertx())
      router <- ZIO.succeed(Router.router(vertx))
      _ <- ZIO.attempt {
        val interpreter = VertxZioServerInterpreter[Any]()
        endpoints.foreach(interpreter.route(_)(runtime)(router))
      }
      server <- ZIO.attempt {
        val httpServerOptions = new HttpServerOptions()
        val httpServer = vertx.createHttpServer(
          httpServerOptions
            .setHost(server.host)
            .setPort(server.port)
        )
        httpServer.requestHandler(router)
      }
      result <- ZIO.scoped(
        ZIO.acquireRelease(
          ZIO.attempt(server.listen()).flatMap(_.asRIO)
        ) { server =>
          ZIO.attempt(server.close()).flatMap(_.asRIO).orDie
        } *> ZIO.never
      )
    } yield result
  }
}
