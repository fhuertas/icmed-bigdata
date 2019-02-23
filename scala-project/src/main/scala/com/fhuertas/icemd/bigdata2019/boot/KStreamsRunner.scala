package com.fhuertas.icemd.bigdata2019.boot

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.fhuertas.icemd.bigdata2019.config.{ ConfigLoader, KafkaStreamsEjNs }
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._
import scala.compat.java8.DurationConverters._
import scala.concurrent.duration._

import scala.collection.JavaConverters._

object KStreamsRunner extends App with LazyLogging {
  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes._

  val config = ConfigFactory.load().getConfig(KafkaStreamsEjNs.RootNs)

  val kafkaConfig = ConfigLoader.loadAsMap(config, Some(KafkaStreamsEjNs.KafkaClient))

  val properties = {
    val p = new Properties()
    p.putAll(kafkaConfig.asJava)
    p
  }

//  val properties = new Properties()
//  properties.put("application.id", "k-stream-app")
//  properties.put("bootstrap.servers", "localhost:19092,localhost:29092,localhost:39092")
//  properties.put("auto.offset.reset", "earliest")

  val inputTopic  = config.getString(KafkaStreamsEjNs.TopicInput)
  val outputTopic = config.getString(KafkaStreamsEjNs.TopicOutput)

  logger.info(s"Word count exercise: Reading from $inputTopic. Result write to $outputTopic")

  val builder                            = new StreamsBuilder()
  val textLines: KStream[String, String] = builder.stream[String, String](inputTopic)

  textLines.to(outputTopic)
  val streams: KafkaStreams = new KafkaStreams(builder.build(), properties)

  streams.cleanUp()

  streams.start()

  // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
  sys.ShutdownHookThread { streams.close(10.seconds.toJava) }
}
