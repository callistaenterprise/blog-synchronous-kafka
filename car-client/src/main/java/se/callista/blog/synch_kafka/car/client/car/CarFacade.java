package se.callista.blog.synch_kafka.car.client.car;

import java.util.concurrent.CompletableFuture;
import se.callista.blog.synch_kafka.car.model.Car;

public interface CarFacade {

  Car getCar(String VIN);

  CompletableFuture<Car> getCarAsync(String VIN);

  CompletableFuture<Car> getCarAsync(String VIN, long timeout);

}