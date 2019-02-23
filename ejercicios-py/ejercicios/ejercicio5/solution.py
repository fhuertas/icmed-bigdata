from pyspark.sql import SparkSession
from pyspark.sql.functions import *

import sys
import re


def build_context():
    builder = SparkSession \
        .builder \
        .appName("StructuredNetworkWordCount")

    if len(sys.argv) > 1:
        builder.master(sys.argv[1])
    return builder.getOrCreate()


def job1():
    spark = build_context()

    lines = spark \
        .readStream \
        .format("socket") \
        .option("host", "localhost") \
        .option("port", 9999) \
        .load()

    # Split the lines into words

    def numbers(string):
        result = re.sub("\\D", "", string)
        return int(0 if result == "" else result)

    remove_chars = udf(numbers)
    words = lines.withColumn("value", remove_chars("value"))

    # Generate running word count
    word_counts = words.withColumn("value", sum("value"))

    query = word_counts \
        .writeStream \
        .outputMode("complete") \
        .format("console") \
        .start()

    query.awaitTermination()


def job2():
    spark = build_context()

    lines = spark \
        .readStream \
        .format("socket") \
        .option("host", "localhost") \
        .option("port", 9999) \
        .load()

    # Split the lines into words

    def only_vocals(string):
        result = re.sub("[^aeiou]*", "", string)
        return "".join(set(result))

    udf_only_vocals = udf(only_vocals)

    words = lines.withColumn("letters-count", length(udf_only_vocals("value")))

    query = words \
        .writeStream \
        .outputMode("append") \
        .format("console") \
        .start()

    query.awaitTermination()


if __name__ == "__main__":
    job2()
