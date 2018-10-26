package se.callista.blog.synch_kafka.car.client.controller;

import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import se.callista.blog.synch_kafka.car.client.car.CarFacade;
import se.callista.blog.synch_kafka.car.model.Car;

@RestController
public class CarController {

  @Autowired
  private CarFacade carFacade;

  @RequestMapping(value = "/car/{vin}", method = RequestMethod.GET,
      produces = {"application/se.callista.blog.synch_kafka.car+json"})
  public DeferredResult<ResponseEntity<Car>> getCar(@PathVariable("vin") String vin) {
    DeferredResult<ResponseEntity<Car>> result = new DeferredResult<>();
    CompletableFuture<Car> reply = carFacade.getCarAsync(vin, 1000L);
    reply.thenAccept(car -> {
      result.setResult(new ResponseEntity<>(car, HttpStatus.OK));
    }).exceptionally(ex -> {
      result.setErrorResult(new ApiException(HttpStatus.NOT_FOUND, ex.getCause().getMessage()));
      return null;
    });
    return result;
  }


  @ExceptionHandler(ApiException.class)
  public final ResponseEntity<ErrorMessage> handleApiException(ApiException ex,
      WebRequest request) {
    HttpStatus status = ex.getStatus();
    ErrorMessage errorDetails = new ErrorMessage().timestamp(ex.getTimestamp())
        .status(status.value()).error(status.getReasonPhrase()).message(ex.getMessage());
    if (request instanceof ServletWebRequest) {
      ServletWebRequest servletWebRequest = (ServletWebRequest) request;
      HttpServletRequest servletRequest =
          servletWebRequest.getNativeRequest(HttpServletRequest.class);
      if (servletRequest != null) {
        errorDetails = errorDetails.path(servletRequest.getRequestURI());
      }
    }
    return new ResponseEntity<>(errorDetails, status);
  }

}
