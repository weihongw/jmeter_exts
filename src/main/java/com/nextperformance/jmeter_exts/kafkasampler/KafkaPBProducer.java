package com.nextperformance.jmeter_exts.kafkasampler;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import rx.Observable;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Kafka binary producer for protobuf message
 */
@ThreadSafe
public class KafkaPBProducer {
    private static final int DEFAULT_MAX_BLOCK_MS = 30000;
    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 1000;
    private static final String DEFAULT_ACK_TYPE = "1";
    private static final int DEFAULT_LINGER_MS = 0;
    private static final int DEFAULT_BATCH_SIZE = 65536;
    private static final int DEFAULT_RETRIES = 1;

    KafkaProducer<byte[], byte[]> kafkaProducer;

    @NotThreadSafe
    public static class BinaryProducerBuilder {
        private final String applicationId;
        private final List<String> brokerList;
        private int maxBlockMs = DEFAULT_MAX_BLOCK_MS;
        private int requestTimeoutMs = DEFAULT_REQUEST_TIMEOUT_MS;
        private String ackType = DEFAULT_ACK_TYPE;
        private int lingerMs = DEFAULT_LINGER_MS;
        private int batchSize = DEFAULT_BATCH_SIZE;
        public int retries = DEFAULT_RETRIES;

        BinaryProducerBuilder(String applicationId, List<String> brokerList) {
            this.applicationId = applicationId;
            this.brokerList = brokerList;
        }

        public BinaryProducerBuilder withAckType(String ackType) {
            this.ackType = ackType;
            return this;
        }

        public BinaryProducerBuilder withMaxBlockMs(int maxBlockMs) {
            checkArgument(maxBlockMs > 0, "maxBlockMs must be > 0, but is set to " + maxBlockMs);
            this.maxBlockMs = maxBlockMs;
            return this;
        }

        public BinaryProducerBuilder withRequestTimeoutMs(int requestTimeoutMs) {
            checkArgument(requestTimeoutMs > 0, "requestTimeoutMs must be > 0, but is set to " + requestTimeoutMs);
            this.requestTimeoutMs = requestTimeoutMs;
            return this;
        }

        public BinaryProducerBuilder withLingerMs(int lingerMs) {
            this.lingerMs = lingerMs;
            return this;
        }

        public BinaryProducerBuilder withBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public BinaryProducerBuilder withRetries(int retries) {
            checkArgument(retries >= 0, "retries must be >= 0, but is set to " + retries);
            this.retries = retries;
            return this;
        }

        public KafkaPBProducer build() {
            return new KafkaPBProducer(this);
        }
    }

    public static BinaryProducerBuilder newBuilder(String applicationId, List<String> brokerList) {
        return new BinaryProducerBuilder(applicationId, brokerList);
    }

    KafkaPBProducer(BinaryProducerBuilder builder) {
        Map<String, Object> conf = new HashMap<>();
        conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, builder.brokerList);
        conf.put(ProducerConfig.CLIENT_ID_CONFIG, builder.applicationId);
        conf.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, builder.requestTimeoutMs);
        conf.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, builder.maxBlockMs);
        conf.put(ProducerConfig.ACKS_CONFIG, builder.ackType);
        conf.put(ProducerConfig.LINGER_MS_CONFIG, builder.lingerMs);
        conf.put(ProducerConfig.BATCH_SIZE_CONFIG, builder.batchSize);
        conf.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        conf.put(ProducerConfig.RETRIES_CONFIG, builder.retries);
        conf.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);
        conf.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        conf.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        kafkaProducer = new KafkaProducer<>(conf);
    }

    public void close() {
        kafkaProducer.close();
    }

    public Observable<RecordMetadata> send(BSLogMessage message, String topic) {
        if (message == null) {
            return Observable.error(new IllegalArgumentException("Message must not be null"));
        }
        if (topic == null) {
            return Observable.error(new IllegalArgumentException("Topic must not be null"));
        }
        if (kafkaProducer == null) {
            return Observable.error(new IllegalArgumentException("The kafka producer must not be null"));
        }
        return Observable.create(subscriber -> {
            if (subscriber.isUnsubscribed()) {
                return;
            }
            try {
                ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topic, message.getId(), message.toByteArray());
                kafkaProducer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        subscriber.onError(exception);
                    } else {
                        subscriber.onNext(metadata);
                        subscriber.onCompleted();
                    }
                });
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
}
