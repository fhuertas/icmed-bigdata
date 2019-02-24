# KSQL

Motor de consultas sobre topics de Kafka
* Los datos tienen que estar estructurados,
* Existen dos tipos de entidades, Streams y Tablas
* Las tablas son mutables por id,
* Los streams son inmutables.
* Compatible (desde la versión de confluet platform 5.0.0) con AVRO, JSON, y CSV
* Existen clientes gráficos y de consola
* Los datos de las consultas se persisten en kafka

Las operaciones no son tan potentes como un motor SQL clásico. Algunas de las peculiaridades de las operaciones son:
* Pueden existir datos en el topic, que si no cumplen la estructura de la tabla/stream, no se utilizan.
* Las operaciones de Join en muchos de los casos necesitan ventanas temporales sobre las que funcionar
* Las tablas solo se pueden unir mediante la clave, nunca mediante otro campo.
* Para muchas operaciones sobre las tablas se utiliza el ultimo campo del identificador como elemento de tabla
* Las uniones necesitan que el número de particiones de las tablas sean las mismas.

## Servicios

Para utilizar estos servicios es necesario tener docker y docker-compose instalado.

Para levantar los servicios es necesario realizar los siguientes comandos, todos
desde la carpeta `other-files/cp-platform_default`

```bash
# Levantar los servicios de Kafka
docker-compose -p cp-platform down; docker-compose -p cp-platform up ksql-server
```

```bash
# Generador de visitas
docker run --network cp-platform_default --rm --name datagen-pageviews  \
     confluentinc/ksql-examples:5.1.0 \
     ksql-datagen \
     bootstrap-server=broker:9092 \
     quickstart=pageviews \
     format=delimited \
     topic=pageviews \
     maxInterval=500
```

```bash
# Generador de información de usuarios
docker run --network cp-platform_default --rm --name datagen-users \
   confluentinc/ksql-examples:5.1.0 \
   ksql-datagen \
   bootstrap-server=broker:9092 \
   quickstart=users \
   format=json \
   topic=users \
   maxInterval=100
```

## Topics, Streams y Tablas

Cliente KSQL

```
# Consola 4: Cliente KSQL
ksql http://localhost:8088
```

### Topics

```
show topics; -- Muestra todos los topics
print "users"; -- Consumidor del topic

```

### Streams

```
SHOW STREAMS; -- Muestra los streams del servidor ksql

CREATE STREAM pageviews_original (viewtime bigint, userid varchar, pageid varchar) WITH \
  (kafka_topic='pageviews', value_format='DELIMITED'); -- Crea un stream

DROP STREAM pageviews_original; -- Borra un stream
```


### Tablas

```
SHOW TABLES; -- Muestra las tablas de ksql

CREATE TABLE users_original (registertime BIGINT, gender VARCHAR, regionid VARCHAR, userid VARCHAR) WITH \
  (kafka_topic='users', value_format='JSON', key = 'userid'); -- crea una tabla

DROP TABLE users_original

```

### Consultas sobre tablas y streams

```
SELECT pageid FROM pageviews_original LIMIT 3;

CREATE STREAM pageviews_enriched AS \
  SELECT users_original.userid AS userid, pageid, regionid, gender \
    FROM pageviews_original \
    LEFT JOIN users_original \
    ON pageviews_original.userid = users_original.userid;

CREATE STREAM pageviews_female AS \
  SELECT * FROM pageviews_enriched \
    WHERE gender = 'FEMALE';

CREATE STREAM pageviews_female_like_89 \
    WITH (kafka_topic='pageviews_enriched_r8_r9') AS \
  SELECT * FROM pageviews_female \
    WHERE regionid LIKE '%_8' OR regionid LIKE '%_9';

CREATE TABLE pageviews_regions \
      WITH (VALUE_FORMAT='avro') AS \
    SELECT gender, regionid , COUNT(*) AS numusers \
    FROM pageviews_enriched \
      WINDOW TUMBLING (size 30 second) \
    GROUP BY gender, regionid \
    HAVING COUNT(*) > 1;

CREATE TABLE pageviews_regions \
      WITH (VALUE_FORMAT='avro') AS \
    SELECT gender, regionid , COUNT(*) AS numusers \
    FROM pageviews_enriched \
      WINDOW TUMBLING (size 30 second) \
    GROUP BY gender, regionid \
    HAVING COUNT(*) > 1;

CREATE TABLE visits_users \
      WITH (VALUE_FORMAT = 'avro') AS \
    SELECT userid, pageid, COUNT(*) AS visits \
    FROM pageviews_original \
      WINDOW TUMBLING (size 1 minute) \
    GROUP BY userid, pageid \
    HAVING COUNT(*) > 1;

```

## Joins

### Carga de datos

```bash
docker run --interactive --rm --network cp-platform_default \
    confluentinc/cp-kafkacat \
    kafkacat -b broker:9092 \
            -t new_orders \
            -K: \
            -P <<EOF
  1:{"order_id":1,"total_amount":10.50,"customer_name":"Bob Smith"}
  2:{"order_id":2,"total_amount":3.32,"customer_name":"Sarah Black"}
  3:{"order_id":3,"total_amount":21.00,"customer_name":"Emma Turner"}
EOF

docker run --interactive --rm --network cp-platform_default \
    confluentinc/cp-kafkacat \
    kafkacat -b broker:9092 \
            -t shipments \
            -K: \
            -P <<EOF
  1:{"order_id":1,"shipment_id":42,"warehouse":"Nashville"}
  3:{"order_id":3,"shipment_id":43,"warehouse":"Palo Alto"}
EOF

docker run --interactive --rm --network cp-platform_default \
  confluentinc/cp-kafkacat \
  kafkacat -b broker:9092 \
          -t warehouse_location \
          -K: \
          -P <<EOF
1:{"warehouse_id":1,"city":"Leeds","country":"UK"}
2:{"warehouse_id":2,"city":"Sheffield","country":"UK"}
3:{"warehouse_id":3,"city":"Berlin","country":"Germany"}
EOF

docker run --interactive --rm --network cp-platform_default \
  confluentinc/cp-kafkacat \
  kafkacat -b broker:9092 \
          -t warehouse_size \
          -K: \
          -P <<EOF
1:{"warehouse_id":1,"square_footage":16000}
2:{"warehouse_id":2,"square_footage":42000}
3:{"warehouse_id":3,"square_footage":94000}
EOF
```

### Join stream-stream

```
SET 'auto.offset.reset' = 'earliest';

CREATE STREAM NEW_ORDERS (ORDER_ID INT, TOTAL_AMOUNT DOUBLE, CUSTOMER_NAME VARCHAR) \
  WITH (KAFKA_TOPIC='new_orders', VALUE_FORMAT='JSON');

CREATE STREAM SHIPMENTS (ORDER_ID INT, SHIPMENT_ID INT, WAREHOUSE VARCHAR) \
  WITH (KAFKA_TOPIC='shipments', VALUE_FORMAT='JSON');
```
```
SELECT ORDER_ID, TOTAL_AMOUNT, CUSTOMER_NAME FROM NEW_ORDERS LIMIT 3

SELECT ORDER_ID, SHIPMENT_ID, WAREHOUSE FROM SHIPMENTS LIMIT 2;
```
```
SELECT O.ORDER_ID, O.TOTAL_AMOUNT, O.CUSTOMER_NAME, \
    S.SHIPMENT_ID, S.WAREHOUSE \
    FROM NEW_ORDERS O \
    INNER JOIN SHIPMENTS S \
      WITHIN 1 HOURS \
      ON O.ORDER_ID = S.ORDER_ID;
```

### Join table-table

```
CREATE TABLE WAREHOUSE_LOCATION (WAREHOUSE_ID INT, CITY VARCHAR, COUNTRY VARCHAR) \
    WITH (KAFKA_TOPIC='warehouse_location', \
          VALUE_FORMAT='JSON', \
          KEY='WAREHOUSE_ID');

CREATE TABLE WAREHOUSE_SIZE (WAREHOUSE_ID INT, SQUARE_FOOTAGE DOUBLE) \
    WITH (KAFKA_TOPIC='warehouse_size', \
          VALUE_FORMAT='JSON', \
          KEY='WAREHOUSE_ID');

SELECT WL.WAREHOUSE_ID, WL.CITY, WL.COUNTRY, WS.SQUARE_FOOTAGE \
    FROM WAREHOUSE_LOCATION WL \
      inner JOIN WAREHOUSE_SIZE WS \
        ON WL.WAREHOUSE_ID=WS.WAREHOUSE_ID \
    LIMIT 3;

```

## Insert into

### Generadores

```bash
docker run --network cp-platform_default --rm  --name datagen-orders-local \
  confluentinc/ksql-examples:5.1.0 \
  ksql-datagen \
      quickstart=orders \
      format=avro \
      topic=orders_local \
      bootstrap-server=broker:9092 \
      schemaRegistryUrl=http://schema-registry:8081
```

```bash
docker run --network cp-platform_default --rm --name datagen-orders_3rdparty \
    confluentinc/ksql-examples:5.1.0 \
    ksql-datagen \
        quickstart=orders \
        format=avro \
        topic=orders_3rdparty \
        bootstrap-server=broker:9092 \
        schemaRegistryUrl=http://schema-registry:8081
```

```
CREATE STREAM ORDERS_SRC_LOCAL \
  WITH (KAFKA_TOPIC='orders_local', VALUE_FORMAT='AVRO');

CREATE STREAM ORDERS_SRC_3RDPARTY \
  WITH (KAFKA_TOPIC='orders_3rdparty', VALUE_FORMAT='AVRO');
```


```
CREATE STREAM ALL_ORDERS AS SELECT 'LOCAL' AS SRC, * FROM ORDERS_SRC_LOCAL;

INSERT INTO ALL_ORDERS SELECT '3RD PARTY' AS SRC, * FROM ORDERS_SRC_3RDPARTY;
```
## Ejercicio propio

A partir de datos sencillos en alguno de los topics, realizar las operaciones descritas anteriormente

### Preparar el entorno



```bash

# Consola 1
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic t1
docker run --interactive --rm --network cp-platform_default   confluentinc/cp-kafkacat   kafkacat -b broker:9092  -t t1 -P <<EOF
1:11,111
2:22,222
3:33,333
1:1:1
2:2:2
1:1,1
2:2,2
3:3,3
3:2,1
2:3,1
1:3,2
EOF
kafka-console-producer --topic t1 \
                       --broker-list broker:9092  \
                       --property parse.key=true \
                       --property key.separator=:

#Consola 2
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic t2
docker run --interactive --rm --network cp-platform_default   confluentinc/cp-kafkacat   kafkacat -b broker:9092  -t t2 -P <<EOF
1:22,333  
2:33,111
3:11,222
1:2,3
2:3,1
3:1,2
EOF
kafka-console-producer --topic t2 \
                       --broker-list broker:9092  \
                       --property parse.key=true \
                       --property key.separator=:
```
