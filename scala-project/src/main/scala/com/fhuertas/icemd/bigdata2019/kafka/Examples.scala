package com.fhuertas.icemd.bigdata2019.kafka
import java.util.Properties

import com.fhuertas.icemd.bigdata2019.dsl.StringDsl._
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }

import scala.collection.JavaConverters._
import scala.compat.java8.DurationConverters._
import scala.concurrent.duration._
import scala.util.Random

object Examples extends App {

  def consumerExample(origen: String, destino: String): Unit = {
    val consumerProperties = new Properties()
    consumerProperties.setProperty("bootstrap.servers",
                                   "follower-1:6667,follower-2:6667,follower-3:6667,leader-1:6667,leader-2:6667")
    consumerProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProperties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProperties.setProperty("group.id", s"${System.currentTimeMillis}")

    val producerProperties = new Properties()
    producerProperties.setProperty("bootstrap.servers", "follower-1:6667,follower-2:6667,follower-3:6667,leader-1:6667,leader-2:6667")
    producerProperties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProperties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](producerProperties)

    val consumer = new KafkaConsumer[Nothing, String](consumerProperties)

    consumer.subscribe(Seq(origen).asJava)

    while (true) {
      val records = consumer.poll(100.millis.toJava)
      records.asScala.foreach(recordOrigin ⇒ {
        val words = wordCount(recordOrigin.value)
        val recordDestino = new ProducerRecord(destino, words.toString, s"$words -> ${recordOrigin.value}")
        producer.send(recordDestino)
      })
    }
  }

  def producerExample(): Unit = {
    val producerProperties = new Properties()
    producerProperties.setProperty("bootstrap.servers", "localhost:9092")
    producerProperties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProperties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[Nothing, String](producerProperties)
    val destino  = "topic-destino"
    while (true) {
      val record = new ProducerRecord(destino, Random.nextInt.toString)
      producer.send(record)
      Thread.sleep(1.second.toMillis)
    }
  }

  consumerExample(args(0),args(1))

}
