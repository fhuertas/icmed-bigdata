# Python base module

## Make commands

* `make env`. Make a environment
* `make clean`. Clean the elements
* `make test-clean`. Clean the tests
* `make test`. make the tests
* `make package`. build the artifact
* `make continuous-test TEST=<test folder> PACKAGE=<package> [ENV=<env folder>]`

## Ejercicio 1. Contar palabras.

Levantar un socket para las pruebas
```
nc -lk 9999
```

Ejecutar el ejemplo. Desde el path de los ejercicios de python

### Modo consola

```
make clean env package
pyspark --py-files dist/ejercicios_python-1.0.0.zip --master spark://localhost:7077
# dentro de la consola de pyspark
from ejercicios.ejercicio5 import solution
solution.main()
```

### Spark submit

```
make clean env package
spark-submit --master spark://localhost:7077 --py-files dist/ejercicios_python-1.0.0.zip \
    ejercicios/ejercicio5/solution.py
```

### Desde el ejecutable de python

En este entorno se puede porque la dependencia de pyspark se encuentra incluida

```
make clean env
source env/bin/activate
# Con el módulo
python -m ejercicios.ejercicio5.solution spark://localhost:7077
# o con el fichero
python python ejercicios/ejercicio5/solution.py spark://localhost:7077
```

### Código del ejemplo

Nota: Este código es compatible con la consola py-spark

```python
# Importando las librerías
from pyspark.sql import SparkSession
from pyspark.sql.functions import explode
from pyspark.sql.functions import split

# Creando el contexto. Aqui se pueden establecer configuración del Job de Spark
# o delegarlas a la llamada de spark-submit
spark = SparkSession \
    .builder \
    .appName("StructuredNetworkWordCount") \
    .getOrCreate()

# Creando el source de datos. y lo transforma en un dataset en streaming
# * readStream: indicando que es Streaming
# * format: Indica que connector se va a usar, entre los posibles valores está
#           kafka, socket, console. etc...
# * option/options: Opciones del conector
# * load. Finaliza la construcción

lines = spark \
    .readStream \
    .format("socket") \
    .option("host", "localhost") \
    .option("port", 9999) \
    .load()

# Operaciones de transformacion y manipulación. este caso divide el texto por " "
# y lo establece como una tabla con la columna word
words = lines.select(
    explode(
        split(lines.value, " ")
    ).alias("word")
)

# Operacion de agregacion por id. une todos los campos en una lista y posteriormente la cuenta
word_counts = words.groupBy("word").count()

# Establece el destino del dataset.
# * writeStream: establece destino en streaming
# * outputMode: Indica como se van actualizar los campos,
#     * complete : Copia todos los datos del dataset cada vez
#     * append: Copia solo los campos nuevos del dataset
#     * update: Copia solo los datos que se han actualizado en el dataset
# * format: donde van a copiarse los datos, al igual que el input indica el connector como
#             kafka.
# * option/options: Indica opciones especificas del conector
# * start: operacion que arranca el proceso de streaming.

query = word_counts \
    .writeStream \
    .outputMode("complete") \
    .format("console") \
    .start()

# Al ser la operacion asycrona, esta llamada espera a que acabe la query
# (que puede no ser hasta que falle)
query.awaitTermination()
```
