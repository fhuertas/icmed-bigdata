package com.fhuertas.icemd.bigdata2019.boot

import java.util.Properties

import com.fhuertas.icemd.bigdata2019.config.{ ConfigLoader, KafkaStreamsEjNs }
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._

import scala.collection.JavaConverters._
import scala.compat.java8.DurationConverters._
import scala.concurrent.duration._

object KStreamsRunner extends App with LazyLogging {
  import com.fhuertas.icemd.bigdata2019.dsl.JsonUtils._
  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes._

  val config = ConfigFactory.load().getConfig(KafkaStreamsEjNs.RootNs)

  val kafkaConfig = ConfigLoader.loadAsMap(config, Some(KafkaStreamsEjNs.KafkaClient))

  val properties = {
    val p = new Properties()
    p.putAll(kafkaConfig.asJava)
    p.put("application.id",s"${System.currentTimeMillis()}-fhuertas-app")
    p
  }

//  val properties = new Properties()
//  properties.put("application.id", s"G${System.currentTimeMillis}")
//  properties.put("bootstrap.servers", "broker:9092")
//  properties.put("auto.offset.reset", "earliest")

  // kafka-streams.topics.input
  val inputTopic = config.getString(KafkaStreamsEjNs.TopicInput)
  // kafka-streams.topics.input2
  val inputTopic2 = config.getString(KafkaStreamsEjNs.TopicInput2)
  // kafka-streams.topics.output
  val outputTopic = config.getString(KafkaStreamsEjNs.TopicOutput)

  logger.info(s"Word count exercise: Reading from $inputTopic. Result write to $outputTopic")

  val builder                         = new StreamsBuilder()
  val tweets: KStream[String, String] = builder.stream[String, String](inputTopic)
  val users: KStream[String, String]  = builder.stream[String, String](inputTopic2)

  val tweetsWithKey = tweets.map((_, body) ⇒ {
    val userId  = body.toJson.path("user.id").extract[String]
    val id      = body.toJson.path("id").extract[String]
    val text    = body.toJson.path("text").extract[String]
    val newBody = s"""{"userId":"$userId","id":"$id","text":"$text"}"""
    (userId, newBody)
  })

  val kUsers = users
    .map((_, body) ⇒ (body.toJson.path("id").extract[String], body))
    .groupByKey
    .reduce((_, R) ⇒ R)

  val resultado = tweetsWithKey.join(kUsers)((left, right) ⇒ s"""{"left":"$left","right":"$right"}""".stripMargin)

  tweetsWithKey.to(s"$outputTopic-tweets")
  kUsers.toStream.to(s"$outputTopic-users")
  resultado.to(s"$outputTopic-join")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), properties)

  streams.cleanUp()

  streams.start()

  // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
  sys.ShutdownHookThread { streams.close(10.seconds.toJava) }
}
