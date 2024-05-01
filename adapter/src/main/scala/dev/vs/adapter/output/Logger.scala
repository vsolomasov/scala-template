package dev.vs.adapter.output

import dev.vs.adapter.input.config.AppConfig
import izumi.logstage.api.IzLogger
import izumi.logstage.api.Log
import izumi.logstage.api.rendering.json.LogstageCirceRenderingPolicy
import izumi.logstage.api.routing.StaticLogRouter
import izumi.logstage.sink.ConsoleSink
import logstage.LogIO
import logstage.LogZIO
import zio.IO
import zio.ZIO
import zio.ZLayer

object Logger:

  val live: ZLayer[AppConfig, Throwable, LogZIO] = ZLayer {
    for {
      logConfig <- ZIO.serviceWith[AppConfig](_.log)
      render <- ZIO.succeed(LogstageCirceRenderingPolicy(logConfig.pretty))
      level <- ZIO.succeed(Log.Level.parseSafe(logConfig.level, Log.Level.Info))
      logger <- ZIO.attempt(IzLogger(level, Seq(ConsoleSink(render))))
      _ <- ZIO.attempt(StaticLogRouter.instance.setup(logger.router))
    } yield LogZIO.withFiberId(logger)
  }
