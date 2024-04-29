package dev.vs.adapter.input.http

import sttp.tapir.ztapir.ZServerEndpoint

trait Endpoint {
  def endpoint: ZServerEndpoint[Any, Any]
}
