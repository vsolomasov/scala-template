package dev.vs.adapter.input.config

import zio.System
import zio.ZLayer

final case class AppInfo(
  name: String,
  version: String,
  instance: String,
  environment: String
):

  val toMap: Map[String, String] = Map(
    "system" -> name,
    "version" -> version,
    "inst" -> instance,
    "env" -> environment
  )

object AppInfo:
  def live(name: String, version: String): ZLayer[Any, Throwable, AppInfo] = ZLayer.fromZIO {
    for {
      instance <- System.propertyOrElse(s"$name.inst", "unknown")
      environment <- System.propertyOrElse(s"$name.env", "unknown")
    } yield AppInfo(name, version, instance, environment)
  }
