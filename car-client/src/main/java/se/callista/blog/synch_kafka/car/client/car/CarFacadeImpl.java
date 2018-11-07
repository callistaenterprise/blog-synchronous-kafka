package se.callista.blog.synch_kafka.car.client.car;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.callista.blog.synch_kafka.car.model.Car;
import se.callista.blog.synch_kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;

@Component
public class CarFacadeImpl implements CarFacade {

  @Autowired
  private CompletableFutureReplyingKafkaTemplate<String,String,Car> requestReplyKafkaTemplate;

  @Override
  public Car getCar(String vin) {
    try {
      return getCarAsync(vin).get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Failed to get Car", e);
    }
  }

  @Override
  public CompletableFuture<Car> getCarAsync(String vin) {
    return requestReplyKafkaTemplate.sendAndReceiveDefault(vin);
  }

}
