package dev.vs.adapter.input.config

import dev.vs.adapter.input.config.AppConfig.ServersConfig
import zio._
import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._

final case class AppConfig(server: ServersConfig)

object AppConfig:
  final case class ServerConfig(host: String, port: Int)
  final case class ServersConfig(system: ServerConfig)

  val live: ZLayer[Any, Config.Error, AppConfig] = ZLayer.fromZIO(
    ConfigProvider.fromResourcePath().load(deriveConfig[AppConfig])
  )
