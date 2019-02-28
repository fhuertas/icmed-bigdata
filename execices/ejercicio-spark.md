# Leyendo tweets.

Leer de dos topics de kafka, uno en streaming y otro en batch y hacer join por el id de
usuario. Finalmente guardar el resultado en formato avro


## Preparativos

##### Kafka
```bash
cd ~/Projects/uah-angvd-2019/docker-services/kafka-multi-broker/
docker-compose -p ejercicios down # si es necesario
docker-compose -p ejercicios up broker-1 broker-2 broker-3
```

#####Poblar topic de usuarios
```bash
# Poblar primer topic usuarios
cd ~/Projects/uah-angvd-2019
kafka-console-producer --broker-list localhost:19092,localhost:29092,localhost:39092 --topic users < files/users
```

### Ejecución

Para ejecutar el ejercicio es necesario indicar los paquetes adicionales que se necesitan. En concreto
son necesarios kafka y avro. Es necesario hacerlo de la siguiente forma.

```bash
# Dentro del contendor creado anteriormente ejecutarlo
spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0,org.apache.spark:spark-avro_2.11:2.4.0 \
             --master local[*] ejercicio6/solution.py
```
Tras arrancar poblar el topic de mensajes

```bash
# Poblar primer topic usuarios
cd ~/Projects/uah-angvd-2019
kafka-console-producer --broker-list localhost:19092,localhost:29092,localhost:39092 --topic tweets < files/messages
```