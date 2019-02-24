package com.fhuertas.icemd.bigdata2019.utils

import java.io.{BufferedReader, FileInputStream, InputStreamReader}
import java.text.SimpleDateFormat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
// /home/fhuertas/Projects/personal/icemd-bigdata/other-files/tweets
object TimeReader extends LazyLogging {

  // "Oct 27, 2014 10:00:00 PM"
  val TwitterTimestampReader = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a")

  case class Event(id: String, date: Long, delay: Long, event: String)
  case class ParserTime(createdAt: String)

  def processLine(bufferReader: BufferedReader, idField: String, timeField: String, dateFormat: String): Seq[Event] = {
    val twitterTimestampReader = new SimpleDateFormat(dateFormat)
    @tailrec
    def processLineRec(br: BufferedReader,
                       tsr: SimpleDateFormat,
                       idField: String,
                       timeField: String,
                       acc: Seq[Event]): Seq[Event] =
      br.readLine match {
        case line: String ⇒
          val previous = acc.headOption
          // parse
          val mapper = new ObjectMapper() with ScalaObjectMapper
          mapper.registerModule(DefaultScalaModule)
          val json = mapper.readValue[Map[String, Object]](line)
          val newEvent = json.get("createdAt").map { date ⇒
            val id          = json.get("id").map(_.toString).orNull
            val currentTime = tsr.parse(date.toString).getTime
            val delay       = previous.map(currentTime - _.date).getOrElse(0L)
            Event(id, currentTime, delay, line)
          }
          processLineRec(br, tsr, idField, timeField, acc ++ newEvent)
        case _ ⇒ acc
      }

    val r = processLineRec(bufferReader, twitterTimestampReader, idField, timeField, Seq.empty)
    logger.info(s"Processed ${r.length} events. In a windowTime: ${r.lastOption.map(_.delay).getOrElse(0L)} ms")
    r
  }

}
