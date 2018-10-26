package se.callista.blog.synch_kafka.car.conf;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import se.callista.blog.synch_kafka.car.controller.RequestReply;
import se.callista.blog.synch_kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;

@Configuration
public class CompletableFutureReplyingKafkaTemplateConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Value("${kafka.topic.car.request}")
  private String requestTopic;

  @Value("${kafka.topic.car.reply}")
  private String replyTopic;

  @Bean
  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

    return props;
  }

  @Bean
  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return props;
  }

  @Bean
  public CompletableFutureReplyingKafkaTemplate<String, RequestReply, RequestReply> replyKafkaTemplate() {
    CompletableFutureReplyingKafkaTemplate<String, RequestReply, RequestReply> requestReplyKafkaTemplate =
        new CompletableFutureReplyingKafkaTemplate<>(requestProducerFactory(),
            replyListenerContainer());
    requestReplyKafkaTemplate.setDefaultTopic(requestTopic);
    return requestReplyKafkaTemplate;
  }

  @Bean
  public ProducerFactory<String, RequestReply> requestProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public ConsumerFactory<String, RequestReply> replyConsumerFactory() {
    JsonDeserializer<RequestReply> jsonDeserializer = new JsonDeserializer<>();
    jsonDeserializer.addTrustedPackages(RequestReply.class.getPackage().getName());
    return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
        jsonDeserializer);
  }

  @Bean
  public KafkaMessageListenerContainer<String, RequestReply> replyListenerContainer() {
    ContainerProperties containerProperties = new ContainerProperties(replyTopic);
    return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
  }

}
