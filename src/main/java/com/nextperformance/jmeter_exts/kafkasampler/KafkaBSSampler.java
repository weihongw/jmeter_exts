package com.nextperformance.jmeter_exts.kafkasampler;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.config.Arguments;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class KafkaBSSampler extends AbstractJavaSamplerClient {

    class ResultSubscriber extends SampleResult {
        Subscriber<RecordMetadata> subscriber = new Subscriber<RecordMetadata>() {
            @Override
            public void onCompleted() {
                return;
            }

            @Override
            public void onError(Throwable e) {
                log.debug(e.getLocalizedMessage());
                return;
            }

            @Override
            public void onNext(RecordMetadata recordMetadata) {
                String data = "topic: " + recordMetadata.topic() + ", partition: "
                        + recordMetadata.partition() + ", offset: "
                        + recordMetadata.offset();
                setSamplerData(data);
                sampleEnd();
            }
        };

        Subscriber<RecordMetadata> getSubscriber() {
            return subscriber;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(KafkaBSSampler.class);

    private KafkaBinaryMsgProducer producer;
    private String topic;
    private double userOnlyPercentage, websiteOnlyPercentage, userAndWebsitePercentage;
    private ArrayList<ResultSubscriber> results = new ArrayList<>();
    private Iterator<ResultSubscriber> resultsIter = results.iterator();
    private final int capacityResults = 1000;
    private int sizeResults = 0;

    @Override
    public void setupTest(JavaSamplerContext context) {
        log.debug(getClass().getName() + ": setupTest");

        String appID = context.getParameter("application ID");
        List<String> hosts = Arrays.asList(context.getParameter("broker list").split(","));
        int requestTimeout = Integer.parseInt(context.getParameter("request.timeout.ms"));
        int maxBlockMs = Integer.parseInt(context.getParameter("max.block.ms"));
        String ackType = context.getParameter("number of acks");

        topic = context.getParameter("topic");

        userOnlyPercentage = Double.parseDouble(context.getParameter("Percentage of user-info only requests"));
        websiteOnlyPercentage = Double.parseDouble(context.getParameter("Percentage of user-info only requests"));
        userAndWebsitePercentage = Double.parseDouble(context.getParameter("Percentage of requests for both user and website info"));

        if (userOnlyPercentage == 0.0 && websiteOnlyPercentage == 0.0 && userAndWebsitePercentage == 0.0) {
            userOnlyPercentage = 1.0/3;
            websiteOnlyPercentage = 1.0/3;
            userAndWebsitePercentage = 1.0/3;
        } else {
            double sum = userOnlyPercentage + websiteOnlyPercentage + userAndWebsitePercentage;
            userOnlyPercentage /= sum;
            websiteOnlyPercentage /= sum;
            userAndWebsitePercentage = 1.0 - userOnlyPercentage - websiteOnlyPercentage;
        }

        try {
            producer = KafkaBinaryMsgProducer.newBuilder(appID, hosts)
                    .withRequestTimeoutMs(requestTimeout)
                    .withMaxBlockMs(maxBlockMs)
                    .withAckType(ackType)
                    .build();

        } catch (Exception ex) {
            log.error(getClass().getName() + ": " + ex.getMessage());
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        log.debug(getClass().getName() + ": teardownTest");
        producer.close();
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultArgs = new Arguments();

        String defaultUuid = "6f9120c9-bc2a-4903-9a0f-84cbcbd03e4f";
        String defaultBidRequest = "{\"id\":\"2520760927708276680\",  " +
                "\"imp\":[{\"id\": \"8b747d53-c030-42d4-95d3-aacafcb0a346\", \"banner\": {\"w\": 300,\"h\": 250 }}],  " +
                "\"site\": {\"id\": \"45335\", \"page\": \"regie.free.fr\"  },  " +
                "\"device\": {\"ua\": \"Mozilla\", \"ip\": \"82.67.77.18\", \"geo\": {\"country\": \"FRA\"}},  " +
                "\"user\": {\"id\": \"5502727545525838854\", \"buyeruid\":\"" + defaultUuid + "\"},  " +
                "\"tmax\": \"5000\",  \"wseat\": [\"Agency1\"],  " +
                "\"bcat\": [\"IAB1-1\"],  \"badv\": [\"google.com\"],  \"cur\": [\"USD\" ]}";

        defaultArgs.addArgument("application ID", "brand safety test");
        defaultArgs.addArgument("broker list",
                "kafka-test01.geu.nextperf.local:9092, kafka-test02.geu.nextperf.local:9092, kafka-test03.geu.nextperf.local:9092");
        defaultArgs.addArgument("max.block.ms", "30000");
        defaultArgs.addArgument("request.timeout.ms", "1000");
        defaultArgs.addArgument("number of acks", "1");
        defaultArgs.addArgument("linger.ms", "0");
        defaultArgs.addArgument("retries", "1");
        defaultArgs.addArgument("batch.size", "65536");
        defaultArgs.addArgument("bid request", defaultBidRequest);
        defaultArgs.addArgument("uuid", defaultUuid);
        defaultArgs.addArgument("Percentage of user-info only requests", "33.3");
        defaultArgs.addArgument("Percentage of website-info only requests", "33.3");
        defaultArgs.addArgument("Percentage of requests for both user and website info", "33.3");
        defaultArgs.addArgument("topic", "brandSafety");

        return defaultArgs;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        ResultSubscriber result = getNextResultSpot();

        result.sampleStart();

        String rawBidRequest = context.getParameter("bid request");
        String uuid = context.getParameter("uuid");

        BSLogMessage msg = null;
        Double dice = Math.random();

        if(dice < userOnlyPercentage)
            msg = KafkaBSMessageBuilder.buildBSLogMessageUserOnly(rawBidRequest, uuid);
        else if(dice < (userOnlyPercentage + websiteOnlyPercentage))
            msg = KafkaBSMessageBuilder.buildBSLogMessageWebsiteOnly(rawBidRequest, uuid);
        else
            msg = KafkaBSMessageBuilder.buildBSLogMessageUserAndWebsite(rawBidRequest, uuid);

        producer.send(msg, topic).subscribe(result.getSubscriber());

        return result;
    }

    private ResultSubscriber getNextResultSpot() {
        ResultSubscriber result = null;

        // recycling the spots
        if (resultsIter.hasNext())
            result = resultsIter.next();
        else if (sizeResults < capacityResults) {
            result = new ResultSubscriber();
            results.add(result);
            resultsIter = results.listIterator(sizeResults);    // end
            sizeResults ++;
        }
        else {
            resultsIter = results.listIterator();               // index 0
            result = resultsIter.next();
        }
        return result;
    }
}