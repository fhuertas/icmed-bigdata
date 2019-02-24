package com.fhuertas.icemd.bigdata2019.utils
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.annotation.tailrec

object JsonUtils {
  implicit val formats: Formats = DefaultFormats

  implicit class JsonStringWrapper(body: String) {
    def toJson: JValue = parse(body)
  }
  implicit class JValueWrapper(json: JValue) {

    def path(path: String): JValue = {
      val listPath = path.split("\\.").toList
      get(json, listPath)
    }
    def asJsonString: String = compact(render(json))

  }

  @tailrec
  private def get(root: JValue, restPath: List[String]): JValue =
    restPath match {
      case x :: xs ⇒ get(root \ x, xs)
      case Nil     ⇒ root
    }
}
