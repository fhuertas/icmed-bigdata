# Leyendo tweets.

Leer de dos topics de kafka, uno en streaming y otro en batch y hacer join por el id de
usuario. Finalmente guardar el resultado en formato avro


## Preparativos

#####Poblar topic de usuarios
```bash
# Poblar primer topic usuarios
cd ~/Projects/uah-angvd-2019
kafka-console-producer --broker-list <kafka> --topic users < files/users
```

```bash
# Poblar topic de mensajes
cd ~/Projects/uah-angvd-2019
kafka-console-producer --broker-list <kafka> --topic messages < files/messages
```

### Ejecución

Para ejecutar el ejercicio es necesario indicar los paquetes adicionales que se necesitan. En concreto
son necesarios kafka y avro. Es necesario hacerlo de la siguiente forma.

Además es necesario copiar el archivo python dentro del cluster

```bash
# Dentro del cluster creado anteriormente ejecutarlo
spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0,org.apache.spark:spark-avro_2.11:2.4.0 \
             --name "python-spark-tweeter" \
             --config spark.sql.streaming.checkpointLocation=/tmp/check-point \
             --master local[*] spark-python.py
```
