package com.fhuertas.icemd.bigdata2019.kafka
import java.util.Properties

import com.fhuertas.icemd.bigdata2019.dsl.StringDsl._
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.JavaConverters._
import scala.compat.java8.DurationConverters._
import scala.concurrent.duration._
import scala.util.Random

object Examples {

  def consumerExample(): Unit = {
    val consumerProperties = new Properties()
    consumerProperties.setProperty("bootstrap.servers", "localhost:9092")
    consumerProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProperties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProperties.setProperty("group.id", s"${System.currentTimeMillis}")
    val consumer = new KafkaConsumer[Nothing, String](consumerProperties)

    consumer.subscribe(Seq("topic-origen").asJava)

    while (true) {
      val records = consumer.poll(100.millis.toJava)
      records.asScala.foreach(record ⇒ println(wordCount(record.value)))
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
}
