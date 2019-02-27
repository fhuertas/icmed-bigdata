from pyspark.sql import SparkSession
from pyspark.sql.functions import *
from pyspark.sql.types import *


def job():
    spark = SparkSession \
        .builder \
        .appName("KafkaTweetsReader").getOrCreate()

    schema_messages = StructType([
        StructField("id", StringType(), True),
        StructField("createdAt", StringType(), True)])

    schema_users = StructType([StructField("id", StringType(), True), StructField("name", StringType(), True)])

    kafka_input = spark \
        .readStream \
        .format("kafka") \
        .option("kafka.bootstrap.servers", "localhost:19092,localhost:29092,localhost:39092") \
        .option("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") \
        .option("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") \
        .option("subscribe", "tweets") \
        .load()

    users = spark.read.format("format") \
        .option("kafka.bootstrap.servers", "localhost:19092,localhost:29092,localhost:39092") \
        .option("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") \
        .option("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") \
        .option("subscribe", "tweets") \
        .load() \
        .withColumn("users_json", from_json('value', schema_users)) \
        .withColumn("id", col('users_json.id'))

    tweets = kafka_input.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)") \
        .withColumn("msg_json", from_json('value', schema_messages)) \
        .withColumn("user_id", col('msg_json.userId'))

    join_df = users.join(tweets, exp("user_id = id"))

    # query = raw_input.writeStream.format("json").option("path", "/tmp/json-dir").start()
    query = tweets \
        .writeStream \
        .outputMode("append") \
        .format("console") \
        .start()

    query.awaitTermination()


if __name__ == "__main__":
    job()
