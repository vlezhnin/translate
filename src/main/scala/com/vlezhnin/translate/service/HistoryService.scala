package com.vlezhnin.translate.service
import java.sql.Timestamp
import java.util.Date

import slick.driver.PostgresDriver.api._
import slick.driver.PostgresDriver.backend.Database

import scala.concurrent.Future

class HistoryService {

  private val db = Database.forConfig("db")

  def addRecord(userId: Long, text: String, timestamp: Long): Future[AnyVal] = {
    val query = sqlu"""INSERT INTO search_log(user_id, text, timestamp) VALUES (${userId}, ${text}, ${new Timestamp(new Date().getTime)})"""
    db.run(query)
  }

}
