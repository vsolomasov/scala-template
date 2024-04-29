package dev.vs.adapter.input.http

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter._
import sttp.tapir.ztapir._
import zio.ZIO
import zio._

object Server {

  def run[R](
    endpoints: List[ZServerEndpoint[Any, Any]]
  )(implicit
    runtime: Runtime[R]
  ): ZIO[Any, Throwable, Unit] = {
    for {
      runtime <- ZIO.runtime
      vertx <- ZIO.succeed(Vertx.vertx())
      router <- ZIO.succeed(Router.router(vertx))
      _ <- ZIO.attempt {
        val interpreter = VertxZioServerInterpreter[Any]()
        endpoints.foreach(interpreter.route(_)(runtime)(router))
      }
      server <- ZIO.attempt {
        val server = vertx.createHttpServer()
        server.requestHandler(router)
      }
      _ <- ZIO.scoped(
        ZIO.acquireRelease(
          ZIO.attempt(server.listen(8080)).flatMap(_.asRIO)
        ) { server =>
          ZIO.attempt(server.close()).flatMap(_.asRIO).orDie
        } *> ZIO.never
      )
    } yield ()
  }
}
