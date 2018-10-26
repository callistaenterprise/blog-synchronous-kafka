package se.callista.blog.synch_kafka.car.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import se.callista.blog.synch_kafka.car.model.Car;
import se.callista.blog.synch_kafka.car.persist.CarRepository;

public class CarController {

  @Autowired
  private CarRepository repository;

  private static final Logger LOGGER = LoggerFactory.getLogger(CarController.class);

  @KafkaListener(topics = "${kafka.topic.car.request}", containerFactory = "requestReplyListenerContainerFactory")
  @SendTo()
  public RequestReply receive(RequestReply requestReply) {
    String vin = (String) requestReply.getRequest();
    LOGGER.info("received request for VIN {} ", vin);
    Car car = repository.getCar(vin);
    RequestReply reply = RequestReply.reply(car);
    LOGGER.info("sending reply {} ", car);
    return reply;
  }
}
