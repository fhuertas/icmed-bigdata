package com.fhuertas.icemd.bigdata2019
import org.apache.kafka.streams.scala.kstream.{ KStream, KTable }

object TwitterFunctions {
  import com.fhuertas.icemd.bigdata2019.dsl.JsonUtils._
  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes._

  def extractUsers(tweets: KStream[String, String]): KTable[String, String] =
    tweets
      .map { (_, body) ⇒
        val json  = body.toJson
        val key   = json.path("user.id").extract[String]
        val value = json.path("user").asJsonString
        (key, value)
      }
      .groupByKey
      .reduce((_, v) ⇒ v)

  def extractMsg(tweets: KStream[String, String]): KStream[String, String] =
    tweets.map { (_, body) ⇒
      val json      = body.toJson
      val user      = json.path("user.id").extract[String]
      val id        = json.path("id").extract[String]
      val createdAt = json.path("createdAt").extract[String]
      val text      = json.path("text").extract[String]
      val newMsg =
        s"""{"id":"$id","userId":"$user","text":"$text","createdAt":"$createdAt"}"""
      (id, newMsg)
    }

  def extractFullUsers(tweets: KStream[String, String]): KTable[String, String] =
    tweets
      .map { (_, body) ⇒
        val json = body.toJson
        val key  = json.path("user.id").extract[String]

        (key, json.path("user").asJsonString)
      }
      .groupByKey
      .reduce((_, r) ⇒ r)

  def changeKeyFromJsonField(tweets: KStream[String, String], idPath: String): KStream[String, String] =
    tweets.map((_, body) ⇒ (body.toJson.path(idPath).extract[String], body))

}
