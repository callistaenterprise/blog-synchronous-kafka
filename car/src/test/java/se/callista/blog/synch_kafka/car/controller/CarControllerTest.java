package se.callista.blog.synch_kafka.car.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.callista.blog.synch_kafka.car.model.Car;
import se.callista.blog.synch_kafka.car.persist.CarRepository;
import se.callista.blog.synch_kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CarControllerTest {

  @Autowired
  private CompletableFutureReplyingKafkaTemplate<String,RequestReply,RequestReply> replyKafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  @MockBean
  private CarRepository repository;

  @ClassRule
  public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "car.request", "car.reply");

  @BeforeClass
  public static void setUpBeforeClass() {
      System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
  }

  @Before
  public void setUp() throws Exception {
    // wait until the partitions are assigned
    for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
        .getListenerContainers()) {
      ContainerTestUtils.waitForAssignment(messageListenerContainer,
          embeddedKafka.getPartitionsPerTopic());
    }
  }

  @Test
  public void testRequestResponse() throws Exception {
    Car car = new Car("vin", "plate");
    Mockito.when(repository.getCar("vin")).thenReturn(car);
    RequestReply request = RequestReply.request("vin");
    RequestReply reply = replyKafkaTemplate.sendAndReceiveDefault(request).get();
    Car actualCar = objectMapper.convertValue(reply.getReply(), Car.class);
    Assert.assertEquals(car, actualCar);
  }

}