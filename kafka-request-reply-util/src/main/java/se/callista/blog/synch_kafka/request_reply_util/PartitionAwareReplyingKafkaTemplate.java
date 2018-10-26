package se.callista.blog.synch_kafka.request_reply_util;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;

/**
 * Specialization of the ReplyingKafkaTemplate to manage the Return Address pattern,
 * i.e. setting the REPLY_TOPIC and REPLY_PARTITION based on the configured reply topic.
 */
public class PartitionAwareReplyingKafkaTemplate<K, V, R> extends ReplyingKafkaTemplate<K, V, R> {

  public PartitionAwareReplyingKafkaTemplate(ProducerFactory<K, V> producerFactory,
      GenericMessageListenerContainer<K, R> replyContainer) {
    super(producerFactory, replyContainer);
  }

  private TopicPartition getFirstAssignedReplyTopicPartition() {
    if (getAssignedReplyTopicPartitions() != null &&
        getAssignedReplyTopicPartitions().iterator().hasNext()) {
      TopicPartition replyPartition = getAssignedReplyTopicPartitions().iterator().next();
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Using partition " + replyPartition.partition());
      }
      return replyPartition;
    } else {
      throw new KafkaException("Illegal state: No reply partition is assigned to this instance");
    }
  }

  private static byte[] intToBytesBigEndian(final int data) {
    return new byte[] {(byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff),
        (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff),};
  }

  @Override
  public RequestReplyFuture<K, V, R> sendAndReceive(ProducerRecord<K, V> record) {
    TopicPartition replyPartition = getFirstAssignedReplyTopicPartition();
    record.headers()
        .add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyPartition.topic().getBytes()))
        .add(new RecordHeader(KafkaHeaders.REPLY_PARTITION,
            intToBytesBigEndian(replyPartition.partition())));
    return super.sendAndReceive(record);
  }

}
