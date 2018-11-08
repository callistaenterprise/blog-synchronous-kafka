package se.callista.blog.synch_kafka.request_reply_util;

import org.springframework.kafka.requestreply.ReplyingKafkaOperations;
import org.springframework.kafka.requestreply.RequestReplyFuture;

public interface PartitionAwareReplyingKafkaOperations<K, V, R> extends ReplyingKafkaOperations<K, V, R> {

    RequestReplyFuture<K, V, R> sendAndReceiveDefault(V data);

    RequestReplyFuture<K, V, R> sendAndReceiveDefault(K key, V data);

    RequestReplyFuture<K, V, R> sendAndReceiveDefault(Integer partition, K key, V value);

    RequestReplyFuture<K, V, R> sendAndReceiveDefault(Integer partition, Long timestamp, K key, V value);

    RequestReplyFuture<K, V, R> sendAndReceive(String topic, V data);

    RequestReplyFuture<K, V, R> sendAndReceive(String topic, K key, V data);

    RequestReplyFuture<K, V, R> sendAndReceive(String topic, Integer partition, K key, V data);

    RequestReplyFuture<K, V, R> sendAndReceive(String topic, Integer partition, Long timestamp, K key, V data);

}