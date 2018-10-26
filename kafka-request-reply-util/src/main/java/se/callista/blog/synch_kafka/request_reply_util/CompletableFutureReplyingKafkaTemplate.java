package se.callista.blog.synch_kafka.request_reply_util;

import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Specialization of the ReplyingKafkaTemplate to adapt the return type to CompletableFuture and
 * streamline the API to resemble the KafkaTemplate.
 */
public class CompletableFutureReplyingKafkaTemplate<K, V, R>
    extends PartitionAwareReplyingKafkaTemplate<K, V, R> {

  private volatile String defaultTopic;

  public CompletableFutureReplyingKafkaTemplate(ProducerFactory<K, V> producerFactory,
      GenericMessageListenerContainer<K, R> replyContainer) {
    super(producerFactory, replyContainer);
  }

  /**
   * The default topic for send methods where a topic is not provided.
   * 
   * @return the topic.
   */
  public String getDefaultTopic() {
    return this.defaultTopic;
  }

  /**
   * Set the default topic for send methods where a topic is not provided.
   * 
   * @param defaultTopic the topic.
   */
  public void setDefaultTopic(String defaultTopic) {
    this.defaultTopic = defaultTopic;
  }

  public CompletableFuture<R> sendAndReceiveDefault(V value) {
    return sendAndReceiveDefault(null, value);
  }

  public CompletableFuture<R> sendAndReceiveDefault(K key, V value) {
    return sendAndReceive(this.defaultTopic, key, value);
  }

  public CompletableFuture<R> sendAndReceive(String topic, V value) {
    return sendAndReceive(topic, null, value);
  }

  public CompletableFuture<R> sendAndReceive(String requestTopic, K key, V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(requestTopic, key, value);
    RequestReplyFuture<K, V, R> reply = super.sendAndReceive(record);
    CompletableFuture<R> completableResult = new CompletableFuture<R>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = reply.cancel(mayInterruptIfRunning);
        super.cancel(mayInterruptIfRunning);
        return result;
      }
    };
    // Add callback to the request sending result
    reply.getSendFuture().addCallback(new ListenableFutureCallback<SendResult<K, V>>() {
      @Override
      public void onSuccess(SendResult<K, V> sendResult) {
        // NOOP
      }
      @Override
      public void onFailure(Throwable t) {
        completableResult.completeExceptionally(t);
      }
    });
    // Add callback to the reply
    reply.addCallback(new ListenableFutureCallback<ConsumerRecord<K, R>>() {
      @Override
      public void onSuccess(ConsumerRecord<K, R> result) {
        completableResult.complete(result.value());
      }
      @Override
      public void onFailure(Throwable t) {
        completableResult.completeExceptionally(t);
      }
    });
    return completableResult;
  }

}
