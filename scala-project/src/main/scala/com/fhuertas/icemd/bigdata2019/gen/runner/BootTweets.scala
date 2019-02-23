package com.fhuertas.icemd.bigdata2019.gen.runner

import java.io.{ BufferedReader, FileInputStream, InputStreamReader }

import com.fhuertas.icemd.bigdata2019.config.KafkaConfigNs.Producer
import com.fhuertas.icemd.bigdata2019.kafka.KafkaBuilder
import com.fhuertas.icemd.bigdata2019.utils.TimeReader
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{ Duration, _ }
import scala.concurrent.{ Await, Future, Promise }
import scala.util.Try

object BootTweets extends App with LazyLogging {

  def delay(time: Long): Try[Future[Nothing]] =
    Try(Await.ready(Promise().future, time millis))

  val TwitterNS = "generator.twitter.file"

  val config      = ConfigFactory.load().getConfig(TwitterNS)
  val kafkaConfig = config.getConfig(Producer)

  val isr = if (args.length == 1) {
    new InputStreamReader(new FileInputStream(args(0)))
  } else {
    new InputStreamReader(getClass.getClassLoader.getResourceAsStream(config.getString("file.name")))
  }
  val br = new BufferedReader(isr)

  val idField    = config.getString("file.field.id")
  val timeField  = config.getString("file.field.time.name")
  val timeFormat = config.getString("file.field.time.format")
  val factor     = config.getLong("factor")
  val topic      = config.getString("topic")

  logger.info(s"""
                 | Configuration
                 |  * idField      : $idField
                 |  * timeField    : $timeField
                 |  * timeFormat   : $timeFormat
                 |  * Speed factor : $factor
                 |  * topic        : $topic
    """.stripMargin)

  lazy val producer = KafkaBuilder.buildProducer[String, String](kafkaConfig)

  logger.info("Reading events")
  val tweets = TimeReader.processLine(br, idField, timeField, timeFormat)
  val time   = tweets.headOption.map(_.delay).map(tweets.last.delay - _).getOrElse(0L) / 1000L / factor
  logger.info(s"Start publish. total time $time s. Events ${tweets.length}")

  val futures = tweets.zipWithIndex.map {
    case (t, k) ⇒
      Future {
        delay(t.delay / 1000000)
        logger.debug(s"Publish ID: $k, delay: ${t.delay}, event: ${t.event}")
        producer.send(new ProducerRecord[String, String](topic, t.id, t.event))
      }
  }
  Await.result(Future.sequence(futures), Duration.Inf)

}
