package com.vlezhnin.translate.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class Index extends Controller {
  get("/:*") { request: Request =>
    response.ok.fileOrIndex(
      request.params("*"),
      "index.html")
  }

}
