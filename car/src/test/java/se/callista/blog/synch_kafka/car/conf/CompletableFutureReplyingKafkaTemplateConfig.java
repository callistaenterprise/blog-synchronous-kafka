package se.callista.blog.synch_kafka.car.conf;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import se.callista.blog.synch_kafka.car.model.Car;
import se.callista.blog.synch_kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;

@Configuration
@Import(KafkaConfig.class)
public class CompletableFutureReplyingKafkaTemplateConfig {

  @Value("${kafka.topic.car.request}")
  private String requestTopic;

  @Value("${kafka.topic.car.reply}")
  private String replyTopic;

  @Autowired
  private KafkaConfig kafkaConfig;

  @Bean
  public CompletableFutureReplyingKafkaTemplate<String, String, Car> replyKafkaTemplate() {
    CompletableFutureReplyingKafkaTemplate<String, String, Car> requestReplyKafkaTemplate =
        new CompletableFutureReplyingKafkaTemplate<>(requestProducerFactory(),
            replyListenerContainer());
    requestReplyKafkaTemplate.setDefaultTopic(requestTopic);
    return requestReplyKafkaTemplate;
  }

  @Bean
  public ProducerFactory<String, String> requestProducerFactory() {
    return new DefaultKafkaProducerFactory<>(kafkaConfig.producerConfigs());
  }

  @Bean
  public ConsumerFactory<String, Car> replyConsumerFactory() {
    JsonDeserializer<Car> jsonDeserializer = new JsonDeserializer<>();
    jsonDeserializer.addTrustedPackages(Car.class.getPackage().getName());
    return new DefaultKafkaConsumerFactory<>(kafkaConfig.consumerConfigs(), new StringDeserializer(),
        jsonDeserializer);
  }

  @Bean
  public KafkaMessageListenerContainer<String, Car> replyListenerContainer() {
    ContainerProperties containerProperties = new ContainerProperties(replyTopic);
    return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
  }

}
