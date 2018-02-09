package com.nextperformance;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

class MyCallback implements Callback {
    static String status;
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        status = (metadata == null) ? exception.getMessage() :
                ("topic: " + metadata.topic() + ", partition: "
                        + metadata.partition() + ", offset: "
                        + metadata.offset());
    }
}