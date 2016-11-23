package com.vlezhnin.translate.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class API extends Controller {
  post("/rest/translate") { request: Request =>
    val text: String = request.getParam("text")
    print(s"Request received with query $text")
    s"Translated $text"
  }

}
