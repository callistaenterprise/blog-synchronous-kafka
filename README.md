# blog-synchronous-kafka
Event Driven Architectures using Apache Kafka are gaining lots of attention lately. If combining Event Notification using Kafka with traditional 
Request-Response, it may be necessary to implement synchronous semantics on top of asynchronous Kafka topics. Here's how to do that using Spring Kafka.

## Project layout

The example is split in two projects: **car** provides a car service, while **car-client** provides a rest api to display car information. The **car-model** contains some common types, while the **kafka-request-reply-util** contains higher-level abstractions on top of Spring Kafka.

### Running the example

A Kafka message backbone is required to run the examples. The docker-compose file provides a convenient way of running a minimal kafka cluster in docker. Start the cluster by running
`docker-compose up -d`.

Then start **car** and **car-client** respectively:

```mvn spring-boot:run```

Access the car-client on `http://localhost:8089/`:

* `curl http://localhost:8089/car/12345678901234567`
