package com.tcc.url_cutter_api.utils;

public class SnowflakeIdGenerator {

    private static final long EPOCH = 1704067200000L; // 2024-01-01

    private static final long NODE_ID_BITS = 10;
    private static final long SEQUENCE_BITS = 12;

    private static final long MAX_NODE_ID = ~(-1L << NODE_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private final long nodeId;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(long nodeId) {
        if (nodeId > MAX_NODE_ID || nodeId < 0)
            throw new IllegalArgumentException("nodeId inválido");

        this.nodeId = nodeId;
    }

    public synchronized long nextId() {

        long timestamp = System.currentTimeMillis();

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;

            if (sequence == 0)
                timestamp = waitNextMillis(timestamp);
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << (NODE_ID_BITS + SEQUENCE_BITS))
                | (nodeId << SEQUENCE_BITS)
                | sequence;
    }

    private long waitNextMillis(long timestamp) {
        while (timestamp == lastTimestamp)
            timestamp = System.currentTimeMillis();

        return timestamp;
    }
}
