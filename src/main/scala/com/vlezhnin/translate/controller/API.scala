package com.vlezhnin.translate.controller

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

class API extends Controller {

  val YANDEX_API_KEY = "trnsl.1.1.20161026T190949Z.7010d055d88c34c4.068b7c9f6a931ea34df15a6f7f6660a24ba36e97"

  val yandexApiClient: Service[Request, Response] = Http.client.withTls("translate.yandex.net").newService("translate.yandex.net:443")

  val danskOrdbogClient: Service[Request, Response] = Http.client.newService("ordnet.dk:80")

  post("/rest/translate") { request: Request =>
    val text: String = request.getParam("text")
    println(s"Request received with query $text")

    val yandexRequestEn = Request(com.twitter.finagle.http.Method.Get, "/api/v1.5/tr.json/translate?key=" + YANDEX_API_KEY + "&lang=da-en&text=" + text)
    val yandexRequestRu = Request(com.twitter.finagle.http.Method.Get, "/api/v1.5/tr.json/translate?key=" + YANDEX_API_KEY + "&lang=da-ru&text=" + text)

    val yandexResponseEn: Future[Response] = yandexApiClient(yandexRequestEn)
    val yandexResponseRu: Future[Response] = yandexApiClient(yandexRequestRu)

    val futures: Future[Seq[Response]] = Future.collect(List(yandexResponseEn, yandexResponseRu))

    futures.map(responses => responses.map(response => response.contentString))
  }

  post("/rest/ordnet") { request: Request =>
    val text: String = request.getParam("text")
    println(s"Request to ordnet received with query $text")

    val danskOrdbogRequest = Request(com.twitter.finagle.http.Method.Get, "/ddo/ordbog/?query=" + text)
    danskOrdbogRequest.host = "ordnet.dk"

    val danskOrdbogResponse: Future[Response] = danskOrdbogClient(danskOrdbogRequest)
    val future = danskOrdbogResponse.map(response => {
      val browser = new JsoupBrowser()
      val doc = browser.parseString(response.contentString)
      val article = doc.body.select(".artikel").head


      val articleMap = scala.collection.mutable.Map[String, Object]()
      articleMap += "wordType" -> article.select(".definitionBoxTop .tekstmedium").map(_.text).headOption.orNull
      articleMap += "bending" -> article.select("#id-boj .tekstmedium").headOption.map(_.text).orNull


      val meanings = article.select("#content-betydninger").headOption.map(meaningBlock => {
        meaningBlock.select(".definitionIndent").map(_.select(".definitionBox").headOption.map(_.text).orNull).filter(_ != null)
      }).orNull
      articleMap += "meanings" -> meanings


      val wordOptions = doc.body.select("#opslagsordBox_expanded .searchResultBox div").map(element => Map(
        "text" -> element.text,
        "link" -> element.select("a").head.attr("href"))
      )

      Map(
        "options" -> wordOptions,
        "article" -> articleMap
      )
    })

    future
  }

}
