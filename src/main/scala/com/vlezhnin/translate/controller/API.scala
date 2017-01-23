package com.vlezhnin.translate.controller

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.finatra.http.Controller
import com.twitter.util.{Await, Duration, Future}

class API extends Controller {

  val YANDEX_API_KEY = "trnsl.1.1.20161026T190949Z.7010d055d88c34c4.068b7c9f6a931ea34df15a6f7f6660a24ba36e97"

  val yandexApiClient: Service[Request, Response] =
    Http.client.withTls("translate.yandex.net")
      .newService("translate.yandex.net:443")

  post("/rest/translate") { request: Request =>
    val text: String = request.getParam("text")
    println(s"Request received with query $text")

    val yandexRequestEn = Request(com.twitter.finagle.http.Method.Get, "/api/v1.5/tr.json/translate?key=" + YANDEX_API_KEY + "&lang=da-en&text=" + text)
    val yandexRequestRu = Request(com.twitter.finagle.http.Method.Get, "/api/v1.5/tr.json/translate?key=" + YANDEX_API_KEY + "&lang=da-ru&text=" + text)

    val yandexResponseEn: Future[Response] = yandexApiClient(yandexRequestEn)
    val yandexResponseRu: Future[Response] = yandexApiClient(yandexRequestRu)

    val future: Future[Seq[Response]] = Future.collect(List(yandexResponseEn, yandexResponseRu))

    val result: Seq[Response] = Await.result(future, Duration.fromSeconds(50))


    result.map(response => response.contentString)
  }

}
