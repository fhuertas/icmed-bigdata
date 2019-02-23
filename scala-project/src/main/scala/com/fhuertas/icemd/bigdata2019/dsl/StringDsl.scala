package com.fhuertas.icemd.bigdata2019.dsl

object StringDsl {
  val wordSplitter: Seq[String] = Seq(" ", ",", ".", "\n", "\t")

  def wordCount(text: String): Int =
    text.replaceAll(s"[${wordSplitter.mkString}]+", " ").trim.split(" ").length
}
