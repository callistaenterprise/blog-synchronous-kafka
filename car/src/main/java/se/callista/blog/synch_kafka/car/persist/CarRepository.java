package se.callista.blog.synch_kafka.car.persist;

import se.callista.blog.synch_kafka.car.model.Car;

public interface CarRepository {

  Car getCar(String vin);

}
