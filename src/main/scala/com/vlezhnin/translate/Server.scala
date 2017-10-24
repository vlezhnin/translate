package com.vlezhnin.translate

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.vlezhnin.translate.controller.{API, Index}
import com.vlezhnin.translate.service.{HistoryService, UserService}

object Main extends Server {}

class Server extends HttpServer {

  val userService = new UserService
  val historyService = new HistoryService

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[CommonFilters]
      .add(new API(historyService, userService))
      .add[Index]
  }
}
