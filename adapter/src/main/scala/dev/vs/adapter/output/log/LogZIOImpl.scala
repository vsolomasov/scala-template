package dev.vs.adapter.output.log

import dev.vs.adapter.input.config.AppConfig
import dev.vs.adapter.input.config.AppInfo
import izumi.logstage.api.IzLogger
import izumi.logstage.api.Log
import izumi.logstage.api.routing.StaticLogRouter
import izumi.logstage.sink.ConsoleSink
import logstage.LogIO
import logstage.LogZIO
import zio.IO
import zio.ZIO
import zio.ZLayer

object LogZioImpl:

  val live: ZLayer[AppInfo & AppConfig, Throwable, LogZIO] = ZLayer {
    for {
      logConfig <- ZIO.serviceWith[AppConfig](_.log)
      appInfo <- ZIO.service[AppInfo]
      render <- ZIO.succeed(CirceRenderingPolicy(logConfig.pretty, appInfo))
      level <- ZIO.succeed(Log.Level.parseSafe(logConfig.level, Log.Level.Info))
      logger <- ZIO.attempt(IzLogger(level, Seq(ConsoleSink(render))))
      _ <- ZIO.attempt(StaticLogRouter.instance.setup(logger.router))
    } yield LogZIO.withFiberId(logger)
  }
