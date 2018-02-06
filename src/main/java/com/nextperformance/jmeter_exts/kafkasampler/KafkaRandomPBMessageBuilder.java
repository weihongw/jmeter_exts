package com.nextperformance.jmeter_exts.kafkasampler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nextperf.log.message.Logs;
import com.nextperf.log.protobuf.CommonHeader;
import com.nextperf.log.protobuf.BrandSafety;

public class KafkaRandomPBMessageBuilder {
    static BrandSafety.BSLogSchema.Builder builder = BrandSafety.BSLogSchema.newBuilder();
    static CommonHeader.Header.Builder headerBuilder = CommonHeader.Header.newBuilder();

    static HashMap<Integer, List<Integer>> updateMapUserOnly = new HashMap<>();
    static HashMap<Integer, List<Integer>> updateMapWebsiteOnly = new HashMap<>();
    static HashMap<Integer, List<Integer>> updateMapUserWebsite = new HashMap<>();

    static {
        updateMapUserOnly.put(1, new ArrayList<Integer>());
        updateMapUserOnly.get(1).add(BrandSafety.BSLogSchema.BSUpdateType.USER_VALUE);

        updateMapWebsiteOnly.put(1, new ArrayList<Integer>());
        updateMapWebsiteOnly.get(1).add(BrandSafety.BSLogSchema.BSUpdateType.WEBSITE_VALUE);

        updateMapUserWebsite.put(1, new ArrayList<Integer>());
        updateMapUserWebsite.get(1).add(BrandSafety.BSLogSchema.BSUpdateType.USER_VALUE);
        updateMapUserWebsite.get(1).add(BrandSafety.BSLogSchema.BSUpdateType.WEBSITE_VALUE);
    }

    private KafkaRandomPBMessageBuilder() {};

    private static BSLogMessage buildBSLogMessage(String rawBidRequest,
                                                 String uuid,
                                                 HashMap<Integer, List<Integer>> updateMap) {
        headerBuilder.setId(Logs.generateLogId());
        builder.setHeader(headerBuilder);

        builder.setBr(rawBidRequest);
        builder.setUuid(uuid);

        for (Map.Entry<Integer, List<Integer>> entry : updateMap.entrySet()) {
            BrandSafety.BSLogSchema.BSPartner.Builder partnerBuilder = BrandSafety.BSLogSchema.BSPartner.newBuilder();
            partnerBuilder.setId(entry.getKey());
            for (Integer updateType : entry.getValue()) {
                partnerBuilder.addBSUpdateTypes(updateType);
            }
            builder.addBSPartners(partnerBuilder);
        }
        return new BSLogMessage(builder);
    }

    public static BSLogMessage buildBSLogMessageUserOnly(String rawBidRequest, String uuid) {
        return buildBSLogMessage(rawBidRequest, uuid, updateMapUserOnly);
    }

    public static BSLogMessage buildBSLogMessageWebsiteOnly(String rawBidRequest, String uuid) {
        return buildBSLogMessage(rawBidRequest, uuid, updateMapWebsiteOnly);
    }

    public static BSLogMessage buildBSLogMessageUserAndWebsite(String rawBidRequest, String uuid) {
        return buildBSLogMessage(rawBidRequest, uuid, updateMapUserWebsite);
    }
}
