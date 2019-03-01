package com.fhuertas.icemd.bigdata2019.gen.runner

import java.io.{ BufferedReader, FileInputStream, InputStreamReader }

import akka.actor.ActorSystem
import com.fhuertas.icemd.bigdata2019.config.KafkaConfigNs.Producer
import com.fhuertas.icemd.bigdata2019.kafka.KafkaBuilder
import com.fhuertas.icemd.bigdata2019.utils.TimeReader
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object BootTweets extends App with LazyLogging {

  val actorSystem                                 = ActorSystem()
  val scheduler                                   = actorSystem.scheduler
  implicit val executor: ExecutionContextExecutor = actorSystem.dispatcher

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

  tweets.zipWithIndex.foreach {
    case (t, k) ⇒
      scheduler.scheduleOnce(t.delay / factor millis)({
        logger.debug(s"Publish ID: $k, delay: ${t.delay}")
        val bodyPrint = if (t.event.length < 80) t.event else s"${t.event.take(80)}..."
        logger.info(s"Event: $bodyPrint")
        producer.send(new ProducerRecord[String, String](topic, t.id, t.event))
      })
  }

  tweets.lastOption
    .map { last ⇒
      scheduler.scheduleOnce((last.delay / factor) + 1000 millis)({
        logger.info("Exiting...")
        System.exit(0)
      })
    }

}
