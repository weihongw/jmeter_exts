package com.nextperformance;

import com.nextperformance.jmeter_exts.kafkasampler.BSLogMessage;
import com.nextperformance.jmeter_exts.kafkasampler.KafkaBinaryMsgProducer;
import com.nextperformance.jmeter_exts.kafkasampler.KafkaBSMessageBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestKafkaBinaryMsgProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaBinaryMsgProducer.class);

    KafkaBinaryMsgProducer pbProducer;
    String topic;

    @Before
    public void init() {
        List<String> hosts = new ArrayList<>();
//        hosts.add("localhost:9092");
        hosts.add("kafka-test01.geu.nextperf.local:9092");

        pbProducer = KafkaBinaryMsgProducer.newBuilder("test", hosts)
                .withRequestTimeoutMs(1000)
                .withMaxBlockMs(30000)
                .withAckType("1")
                .build();

        topic = "test";
    }

    @After
    public void tearDown() {
        pbProducer.close();
    }

//    @Ignore("This test should be ignored unless Kafka is run locally")
    @Test
    public void testSend() {
        String br = "{\"id\":\"2520760927708276680\",  " +
                "\"imp\":[{\"id\": \"8b747d53-c030-42d4-95d3-aacafcb0a346\", \"banner\": {\"w\": 300,\"h\": 250 }}],  " +
                "\"site\": {\"id\": \"45335\", \"page\": \"regie.free.fr\"  },  " +
                "\"device\": {\"ua\": \"Mozilla\", \"ip\": \"82.67.77.18\", \"geo\": {\"country\": \"FRA\"}},  " +
                "\"user\": {\"id\": \"5502727545525838854\", \"buyeruid\":\"6f9120c9-bc2a-4903-9a0f-84cbcbd03e4f\"},  " +
                "\"tmax\": \"5000\",  \"wseat\": [\"Agency1\"],  \"bcat\": [\"IAB1-1\"],  \"badv\": [\"google.com\"],  " +
                "\"cur\": [\"USD\" ]}";

        String uuid = "6f9120c9-bc2a-4903-9a0f-84cbcbd03e4f";

        try{
            Field producer = KafkaBinaryMsgProducer.class.getDeclaredField("kafkaProducer");
            producer.setAccessible(true);
            KafkaProducer p = (KafkaProducer)producer.get(pbProducer);

            BSLogMessage message = KafkaBSMessageBuilder.buildBSLogMessageUserOnly(br, uuid);

            ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topic, message.getId(), message.toByteArray());
            p.send(record, new MyCallback());

            Thread.sleep(20);

            log.debug(MyCallback.status);

            assertNotNull(MyCallback.status);
            assertTrue(MyCallback.status.startsWith(topic));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
