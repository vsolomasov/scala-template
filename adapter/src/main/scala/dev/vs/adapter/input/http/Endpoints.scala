package dev.vs.adapter.input.http

import sttp.tapir.ztapir.ZServerEndpoint

trait Endpoints {
  def endpoints: List[ZServerEndpoint[Any, Any]]
}
