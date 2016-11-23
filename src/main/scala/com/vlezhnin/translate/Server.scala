package com.vlezhnin.translate

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.vlezhnin.translate.controller.{API, Index}

object Main extends Server {}

class Server extends HttpServer {
  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[CommonFilters]
      .add[API]
      .add[Index]
  }
}
