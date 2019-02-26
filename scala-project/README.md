# icemd-bigdata

## run
Generadores

```bash
sbt -mem 2048 -Dgenerator.twitter.file.factor=1 \
    -Dgenerator.twitter.file.kafka.producer.bootstrap.servers=localhost:19092,localhost:29092,localhost:39092 \
    -Dgenerator.twitter.file.topic=users \
    "runMain com.fhuertas.icemd.bigdata2019.gen.runner.BootTweets ../other-files/tweets"
# O
java -cp target/scala-2.11/icemd-bigdata_20190224_180416.jar \
      -Dgenerator.twitter.file.topic=tweets-1 -Dgenerator.twitter.file.factor=12 \
      -Dgenerator.twitter.file.kafka.producer.bootstrap.servers=localhost:19092,localhost:29092,localhost:39092 \
      com.fhuertas.icemd.bigdata2019.gen.runner.BootTweets ../other-files/tweets
```
