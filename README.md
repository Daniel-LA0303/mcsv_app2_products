# Inventory
### Introducción
En este proyecto aplicamos la arquitectura de microservicios con Spring Boot, es un proyecto de inventario, donde el usuario puede agregar productos desde un microservicio del mismo nombre, agregarlos a un inventario y hacer ordenes de esos productos, a su vez cuando se hace una orden el microservicio de orden se comunicará con el microservicio de inventario coroborando que haya disponibilidad del producto, una vez hecho esto utilizamos un message broker, en este caso apache kafka para que haya comunicación, esto sen hizo solo para probar como funcionaban los message brokers, toda la aplicación tiene seguridad con Keycloak por lo que se debe estar autenticado para poder hacer ciertas acciones.
Utilizamos monitoreo con Grafana y Prometheus para vez el rendimiento de toda la aplicación y por ultimo agregamos un Docker compose, esto por que tenemos servicios que necesitan un servidor por lo que opté por un compose para estos servicios.

### 1. Architecture
La arquitectura de microservicios de este proyecto se puede apreciar mejor en la siguiente imagen.
En este proyecto se manejaron varios módulos Spring y servicios que son los siguientes

- Spring Boot
- Spring Data
- Spring Security
- Spring Cloud
- Spring RESTful APIs
- Sleuth
- Zookeper
- Kafka
- Zipkin
- KeyCloak
- Grafana
- Prometheus
- Docker Compose
  

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/1_architecture.png)

### 2. Services APIs
Para la creación de los servicios ocupamos APIs con las capas ofrecidas por Spring Boot. 
Con esto crearemos las APIs de tres servicios que son: Order, Inventory y Producto, estos servicios tendrán funciones CRUD en diferentes bases de datos como lo muestra la arquitectura

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/2_model.png)

### 3. Eureka Server
Eureka es un servidor para el registro y localización de microservicios, equilibrio de carga y tolerancia a fallos, la función de Eureka es registrar las diferentes instancias de microservicios existentes, su ubicación, estado de metadatos, etc.
En nuestro caso empezaremos a registrar estos tres servicios, aunque en el futuro serán más tal como se mostraba en la imgen de la arquitectura al principio.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/3_server.png)

### 4. Comunicación entre microservicios
La comunicación entre microservicios es importante ya que habrá casos en los que deberemos hacerlo ya sea para enviar o recibir información, en nuestro caso nos comunicaremos de dos maneras, para comunicar al microservicio de
Order a Inventory será con WebClient que es programación reactiva, mientras en otros servicios como de Order a Notification, se comunicara con Kafka.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/4_communication.png)

### 5. Implementando un Load Balancer
Aplicaremos un equilibrador de carga con el microservicio Inventory, esto significa que tendremos más de una instancia de este microsorservicio, por lo que el equilibrador de carga será responsable de gestionar las solicitudes que provengan del microservicio Order.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/5_load_balancer.png)

### 6. Implementando un API Gateway
Es el administrador de tráfico que interactúa con los datos o el servicio de fondo real y aplica políticas, autenticación y control de acceso general para las llamadas de API para proteger datos valiosos. Una API Gateway es la forma de controlar el acceso a sus sistemas y servicios de backend y fue diseñado para optimizar la comunicación entre clientes externos y servicios de backend.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/6_api_gateway.png)

### 7. Implementando un Circuit Breaker
Es un problema común que en arquitecturas distribuidas algunos componentes fallan lo que implica que ciertas operaciones dejan de funcionar y es aconsejable tener en cuenta que haríamos en caso de que ciertos microservicios caigan.

En este caso utilizaremos este patrón de diseño para el microservicio de Order, esto con el fin de saber si algo ha fallado en el microservicio de Inventory o Producto, en caso de que si lo hay, pongo un controlador que se ejecuta cuando la comunicación falla aplicando configuraciones de actuator y resilience4j.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/7_circuit_breaker.png)

### 8. Implementando Auth Service
Aplico un microservicio de autenticación para los usuarios, esto significa que un usuario debe autenticarse para hacer uso de la aplicación, todo esto aplicando un servidor de Keycloak, esta herramienta permite hacer autenticación, y al hacerlo devuelve un token que sera vital para hacer uso de la aplicación.




### 9. Docker
Con ayuda de un compose he agregado algunos servicios que necesitaré, tal es el caso de Keycloak entre otros

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/9_docker.png)


### 10. Implementando Trazas
Es muy importante para detectar latencias y problemas en nuestro sistema distribuido y es especialmente útil cuando nuestro sistema se encuentra instalado en distintas data centers o incluso distintas regiones 
Para eso tendremos en cuenta algunas librerías, empezamos con la dependencia de Sleuth, cuando un servicio se comunica con otro, permite identificar ese request cuando hay comunicación.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/12_sleuth.png)

#### Partes de una traza
- Nombre del servicio
- Id de la traza: será un id fijo durante toda la comunicación
- Id de span: es un id que va a cambiar en cada etapa, supongamos que va de servicio a a b, tendrá id inicial y uno final, en caso de que haya dos comunicaciones, habrá tres id diferentes
- Exportación la traza a zipkin que se representa con un boleano

#### Anotaciones
- cs (Client Sent): El cliente ha hecho una petición, inicio del span.
- sr (Server Received): El servidor ha recibido la petición y empieza su procesado timestampsr - timestampcs = latencia de red.
- ss (Server Sent): Envío a cliente desde el servidor de la respuesta timestamps - timestampsr = tiempo de procesamiento de petición en servidor.
- cr (Client Received): Fin del Span, el cliente ha recibido correctamente la respuesta del servidor timestampcs - timestampcr = tiempo total de la petición.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/13_tra.png)


### 11. Visualización de trazas con Zipkin
Ocupamos esta herramienta para poder ver las trazas registradas, es decir, la parte número 4 del arreglo de información de la traza es un booleano, si esta en true significa que se verá en la interfaz de zipkin, es importante mencionar que esto requiere de un servidor por lo que usaremos docker para lograr esto.

![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/14_arch.png)

Interfaz del servidor Zipkin
![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/10_zipkin.png)

### 12. Apache Kafka
Es una plataforma de transmisión de datos distribuidos que pude publicar, suscribirse, almacenar y proceder transmisiones de registros en tiempo real, está diseñado para manejar flujos de datos de múltiples fuentes y entregarlos a múltiples consumidores.

Arquitectura
![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/15_kafka.png)

Kluster Kafka: es donde estará todo nuestro servidor de kafka
Broker: podemos tener uno o más brokers dentro del servidor de kafka
Topic: serán temas de errores, creación etc
Partitions: podemos tener n partitions y es donde se guardan los mensajes 
Replicas: si tenemos un mensaje dentro de un topic, podemos replicar ese mensaje dentro de otro topic o broker, en caso de que se caiga el broker o topic podemos decir que tenemos un respaldo, todo esto se encargaría kafka.

¿Por qué necesitamos zookeeper?
Ordena y gestiona los clusters o brokers, también notifica al consumers y producer de nuevos brokers

Por lo tanto nosotros la utilizamos de manera sencilla, ya que solo comunicamos dos microservicios con Kafka

### 13. Monitoreo con Grafana y Prometheus
Podemos monitorear nuestros microservicios con prometheus, ya que tiene la capacidad de almacenar datos y registrar métricas y grafana es la interfaz donde muestra todas estas métricas y los datos que provienen de prometheus.

Prometheus es más para las métricas mientras que actuator que es una dependencia nos dira información sobre errores, cambios etc, esta información la formatea para que prometheus pueda interpretarla.

Ahora con ayuda de grafana y uso de una interfaz podemos obtener toda la información que tengamos en Prometheus sobre nuestra app, así para mostrarala con alguna interfaz que tenga Grafana
![Texto alternativo](https://github.com/Daniel-LA0303/mcsv_app2_products/blob/main/assets/11_grafana.png)
