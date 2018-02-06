package com.nextperformance.jmeter_exts.kafkasampler;

import com.nextperf.log.message.MessageMeta;
import com.nextperf.log.protobuf.BrandSafety;
import com.nextperf.log.message.binary.AbstractBinaryMessage;

public class BSLogMessage extends AbstractBinaryMessage<BrandSafety.BSLogSchema> {

    BSLogMessage(byte[] rawMessage) {
        super(BrandSafety.BSLogSchema.getDefaultInstance().getParserForType(), rawMessage);
    }

    BSLogMessage(BrandSafety.BSLogSchema message) {
        super(message);
    }

    public BSLogMessage(BrandSafety.BSLogSchema.Builder builder) {
        this(builder.build());
    }

    @Override
    public MessageMeta getType() {
        return MessageMeta.create(MessageMeta.MessageType.BRAND_SAFETY);
    }
}