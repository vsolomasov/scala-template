package dev.vs.adapter.input.http.server

import dev.vs.adapter.input.config.AppConfig.ServerConfig
import dev.vs.adapter.input.http.Endpoints
import zio._

trait Server {

  def run[R](
    config: ServerConfig,
    endpoints: Endpoints
  )(implicit
    runtime: Runtime[R]
  ): ZIO[Scope, Throwable, Unit]
}
