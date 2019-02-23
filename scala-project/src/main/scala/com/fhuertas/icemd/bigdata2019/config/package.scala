package com.fhuertas.icemd.bigdata2019

package object config {
  object KafkaConfigNs {
    val Producer  = "kafka.producer"
    val TotalTime = "time.total"
    val BatchTime = "time.batch"

    val MediaWords       = "words.media"
    val DeviationWords   = "words.deviation"
    val ElementsPerBatch = "elements.per.batch"
    val TopicName        = "topic"
  }

  object KafkaStreamsEjNs {
    val RootNs = "kafka-streams"
    val KafkaClient = "kafka-clients"
    val TopicInput = "topics.input"
    val TopicOutput = "topics.output"
  }

}
