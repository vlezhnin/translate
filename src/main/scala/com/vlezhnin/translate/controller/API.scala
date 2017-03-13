package com.vlezhnin.translate.controller

import java.util.{Collections, Date}

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.{GoogleIdToken, GoogleIdTokenVerifier}
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import com.typesafe.config.{Config, ConfigFactory}
import com.vlezhnin.translate.model.User
import com.vlezhnin.translate.service.{HistoryService, UserService}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.ExecutionContext.Implicits.global

class API(val historyService: HistoryService, val userService: UserService) extends Controller {

  private val conf: Config  = ConfigFactory.load()

  val YANDEX_API_KEY: String = conf.getString("yandex.key")

  val DEFAULT_SECOND_LANGUAGE = "ru"

  val GOOGLE_CLIENT_ID: String = conf.getString("google.clientId")

  val yandexApiClient: Service[Request, Response] = Http.client.withTls("translate.yandex.net").newService("translate.yandex.net:443")

  val danskOrdbogClient: Service[Request, Response] = Http.client.newService("ordnet.dk:80")

  val jsonFactory = new JacksonFactory()

  val transport: HttpTransport  = new NetHttpTransport()

  get("/rest/version") { request: Request =>
    "0.0.3"
  }

  post("/rest/translate") { request: Request =>
    val text: String = request.getParam("text")

    if (request.containsParam("token")) {
      val idTokenString = request.getParam("token")
      val verifier: GoogleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
        .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
        .build()

      val idToken: GoogleIdToken = verifier.verify(idTokenString)
      if (idToken != null) {
        val payload: Payload  = idToken.getPayload

        val userId: String = payload.getSubject

        userService.getByEmail(payload.getEmail).flatMap(user => {
          if (user.isDefined) {
            scala.concurrent.Future {user.get.id}
          } else {
            userService.saveUser(new User(payload.getEmail, payload.get("name").toString, 1l))
          }
        }).flatMap(id => {
          historyService.addRecord(id, text, new Date().getTime)
        }).recover({
          case error: Throwable => println(error)
        })

        println(s"User ID: $userId")

        println(s"User email ${payload.getEmail}")
        println(s"User name ${payload.get("name")}")
        println(s"Email verified ${payload.getEmailVerified}")
      } else {
        println("Invalid ID token.")
      }
    }


    val secondLang: String = request.getParam("lang", DEFAULT_SECOND_LANGUAGE)
    println(s"Request received with query $text")

    val yandexRequestEn = Request(com.twitter.finagle.http.Method.Get, s"/api/v1.5/tr.json/translate?key=$YANDEX_API_KEY&lang=da-en&text=$text")
    val yandexRequestSecond = Request(com.twitter.finagle.http.Method.Get, s"/api/v1.5/tr.json/translate?key=$YANDEX_API_KEY&lang=da-$secondLang&text=$text")

    val yandexResponseEn: Future[Response] = yandexApiClient(yandexRequestEn)
    val yandexResponseRu: Future[Response] = yandexApiClient(yandexRequestSecond)

    val futures: Future[Seq[Response]] = Future.collect(List(yandexResponseEn, yandexResponseRu))

    futures.map(responses => responses.map(response => response.contentString))
  }

  post("/rest/ordnet") { request: Request =>
    val text: String = request.getParam("text")
    val link: String = request.getParam("link")
    println(s"Request to ordnet received with query $text, link $link")

    val danskOrdbogRequest =
    if (link != null && !link.trim.isEmpty) {
      val queryPart = link.substring(link.indexOf('?'))
      Request(com.twitter.finagle.http.Method.Get, s"/ddo/ordbog/$queryPart")
    } else {
      Request(com.twitter.finagle.http.Method.Get, s"/ddo/ordbog/?query=$text")
    }

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
