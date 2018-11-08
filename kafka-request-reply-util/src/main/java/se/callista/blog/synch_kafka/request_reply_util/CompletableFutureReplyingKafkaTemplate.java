package se.callista.blog.synch_kafka.request_reply_util;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Specialization of the ReplyingKafkaTemplate to adapt the return type to CompletableFuture.
 */
public class CompletableFutureReplyingKafkaTemplate<K, V, R> extends PartitionAwareReplyingKafkaTemplate<K, V, R>
    implements CompletableFutureReplyingKafkaOperations<K, V, R> {

  public CompletableFutureReplyingKafkaTemplate(ProducerFactory<K, V> producerFactory,
      GenericMessageListenerContainer<K, R> replyContainer) {
    super(producerFactory, replyContainer);
  }

  @Override
  public CompletableFuture<R> requestReplyDefault(V value) {
    return adapt(sendAndReceiveDefault(value));
  }

  @Override
  public CompletableFuture<R> requestReplyDefault(K key, V value) {
    return adapt(sendAndReceiveDefault(key, value));
  }

  @Override
  public CompletableFuture<R> requestReplyDefault(Integer partition, K key, V value) {
    return adapt(sendAndReceiveDefault(partition, key, value));
  }

  @Override
  public CompletableFuture<R> requestReplyDefault(Integer partition, Long timestamp, K key, V value) {
    return adapt(sendAndReceiveDefault(partition, timestamp, key, value));
  }

  @Override
  public CompletableFuture<R> requestReply(String topic, V value) {
    return adapt(sendAndReceive(topic, value));
  }

  @Override
  public CompletableFuture<R> requestReply(String topic, K key, V value) {
    return adapt(sendAndReceive(topic, key, value));
  }

  @Override
  public CompletableFuture<R> requestReply(String topic, Integer partition, K key, V value) {
    return adapt(sendAndReceive(topic, partition, key, value));
  }

  @Override
  public CompletableFuture<R> requestReply(String topic, Integer partition, Long timestamp, K key, V value) {
    return adapt(sendAndReceive(topic, partition, timestamp, key, value));
  }

  private CompletableFuture<R> adapt(RequestReplyFuture<K, V, R> requestReplyFuture) {
    CompletableFuture<R> completableResult = new CompletableFuture<R>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = requestReplyFuture.cancel(mayInterruptIfRunning);
        super.cancel(mayInterruptIfRunning);
        return result;
      }
    };
    // Add callback to the request sending result
    requestReplyFuture.getSendFuture().addCallback(new ListenableFutureCallback<SendResult<K, V>>() {
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
    requestReplyFuture.addCallback(new ListenableFutureCallback<ConsumerRecord<K, R>>() {
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
