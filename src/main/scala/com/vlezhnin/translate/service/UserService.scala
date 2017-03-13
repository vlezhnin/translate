package com.vlezhnin.translate.service

import com.vlezhnin.translate.model.User
import slick.driver.PostgresDriver.api._
import slick.driver.PostgresDriver.backend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService {

  private val db = Database.forConfig("db")

  def getByEmail(email: String): Future[Option[User]] = {
    val query = sql"""SELECT u.email, u.name, u.id FROM "user" u WHERE u.email=${email}""".as[(String, String, Long)]
    db.run(query).map(tuple => {
      if (tuple.isEmpty) {
        None
      } else {
        Some(new User(tuple.head._1, tuple.head._2, tuple.head._3))
      }
    })
  }

  def saveUser(user: User): Future[Long] = {
    val query =
      sql"""INSERT INTO "user"(email, name) VALUES (${user.email}, ${user.name})
         returning id
          """.as[Long]
    db.run(query).map(rs => rs.head)
  }

}
