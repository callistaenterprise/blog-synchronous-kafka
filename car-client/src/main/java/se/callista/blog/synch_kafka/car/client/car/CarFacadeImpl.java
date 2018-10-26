package se.callista.blog.synch_kafka.car.client.car;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.callista.blog.synch_kafka.car.controller.RequestReply;
import se.callista.blog.synch_kafka.car.model.Car;
import se.callista.blog.synch_kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;

@Component
public class CarFacadeImpl implements CarFacade {

  @Autowired
  private CompletableFutureReplyingKafkaTemplate<String,RequestReply,RequestReply> requestReplyKafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  
  @Override
  public Car getCar(String vin) {
    try {
      return getCarAsync(vin).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Failed to get Car", e);
    }
  }

  private static final long DEFAULT_REPLY_TIMEOUT = 5000L;

  @Override
  public CompletableFuture<Car> getCarAsync(String vin) {
    return getCarAsync(vin, DEFAULT_REPLY_TIMEOUT);
  }

  @Override
  public CompletableFuture<Car> getCarAsync(String vin, long timeout) {
    RequestReply request = RequestReply.request(vin);
    requestReplyKafkaTemplate.setReplyTimeout(timeout);
    CompletableFuture<RequestReply> replyFuture =
        requestReplyKafkaTemplate.sendAndReceiveDefault(request);
    CompletableFuture<Car> completableResult = replyFuture.thenApply(reply -> {
        return objectMapper.convertValue(reply.getReply(), Car.class);
    });
    return completableResult;
  }

}
