# Kafka Streams

Motor de transformaciones sobre kafka
* Existen dos tipos de entidades, Streams y Tablas
* Las tablas son mutables por id,
* Los streams son inmutables.
* No necesita mas infraestructura que el propio kafka
* Las transformaciones son ejecutadas dentro del cluster de kafka
* Soporta operaciones con ventanas

## Ejercicio

### Generadores de datos

```bash
# Consola 2: Generador de visitas
ksql-datagen bootstrap-server=localhost:19092 quickstart=pageviews format=delimited topic=pageviews maxInterval=500
```

```bash
# Consola 3: Generador de información de usuarios
ksql-datagen bootstrap-server=localhost:19092 quickstart=users format=json topic=users maxInterval=100
```

```bash
# Generador de tweets
java -cp icemd-bigdata.jar \
 -Dgenerator.twitter.file.topic=tweets-1 \
 -Dgenerator.twitter.file.factor=1 \
 -Dgenerator.twitter.file.kafka.producer.bootstrap.servers=<connectionString> \
 com.fhuertas.icemd.bigdata2019.gen.runner.BootTweets <pathToFile>
```
### Prarar el programa

```bash
# generar el jar
sbt clean assembly
# Copiar el jar a la maquian ssh
scp target/scala-2.11/icemd-bigdata.jar <vuestras credenciales de la maquina del cluster>
# Ejecutar dentro de la maquina del cluster
java -cp icemd-bigdata.jar \
  [<opciones>] \
  com.fhuertas.icemd.bigdata2019.boot.KStreamsRunner

```
