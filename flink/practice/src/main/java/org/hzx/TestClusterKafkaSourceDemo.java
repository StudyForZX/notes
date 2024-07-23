package org.hzx;

import java.net.URI;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.hzx.UserProcessWindowFunction.MyProcessWindowFunction;
import org.hzx.UserProcessWindowFunction.;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

public class TestClusterKafkaSourceDemo {

    public static String userName = "normal";
    public static String password = "normal-secret";

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // env.enableCheckpointing(10000);
        // env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        // env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        // 取消任务，不删除checkpoint
        // env.getCheckpointConfig().setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // checkpoint超时事件
        // env.getCheckpointConfig().setCheckpointTimeout(6000L);
        // 配置checkpoint的间歇时间
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000L);

        // 强烈建议将 Kafka 的事务超时时间调整至远大于 checkpoint 最大间隔 + 最大重启时间，否则 Kafka 对未提交事务的过期处理会导致数据丢失。
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                    .setBootstrapServers("10.107.70.242:8082")
                    .setGroupId("hawking-api-stat-test")
                    .setTopics("hawking.test")
                    .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
                    // .setDeserializer(new KafkaRecordDeserializationSchemaImplements())
                    .setValueOnlyDeserializer(new SimpleStringSchema())
                    .setProperty("enable.auto.commit", "true") // 关闭自动提交,避免与`checkpoint`机制冲突
                    .setProperty("isolation.level", "read_committed")
                    .setProperty("partition.discovery.interval.ms", "10000") // 定期检查新分区 ms
                    .setProperty("security.protocol", "SASL_PLAINTEXT")
                    .setProperty("sasl.mechanism", "PLAIN")
                    .setProperty("sasl.jaas.config", String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", userName, password))
                    .build();

        WatermarkStrategy<Tuple2<Long, String>> ws = WatermarkStrategy.
            <Tuple2<Long, String>>forBoundedOutOfOrderness(Duration.ofSeconds(5)).
            withTimestampAssigner((element, recordTimestamp) -> element.f0);


        // 逻辑计算
        env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafka-source")
            .map(new KafkaMessageMap())
            .assignTimestampsAndWatermarks(ws)
            .keyBy(v -> v.f1)
            .window(SlidingEventTimeWindows.of(Time.minutes(1), Time.seconds(20)))
            // .allowedLateness(Time.seconds(5))
            .aggregate(new CountApiNo(), new MyProcessWindowFunction())
            .print();

        env.execute();
    }

    /**
     * kafka序列化设置
     */
    // private static class KafkaRecordDeserializationSchemaImplements implements KafkaRecordDeserializationSchema<String> {

    //     @Override
    //     public TypeInformation<String> getProducedType() {
    //         return BasicTypeInfo.STRING_TYPE_INFO;
    //     }

    //     @Override
    //     public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<String> out) throws IOException {
    //         out.collect("key: " + new String(record.key()) + "** value: " + new String(record.value()));
    //     }
    // }

    /**
     * kafka消息解析
     */
    private static class KafkaMessageMap implements MapFunction<String, Tuple2<Long, String>> {
        @Override
        public Tuple2<Long, String> map(String value) throws Exception {

            String url = "";
            long timestamp = 0L;
            String urlRegex = ".*: (?<timestamp>\\d{2}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) uri\\[(?<uri>.*?)\\].*";
            Matcher matcher = Pattern.compile(urlRegex).matcher(value);

            if (matcher.matches()) {

                String timestampStr = matcher.group("timestamp");
                String uriStr = matcher.group("uri");

                DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
                timestamp = LocalDateTime.parse(timestampStr, ofPattern).toEpochSecond(ZoneOffset.ofTotalSeconds((int) Duration.ofHours(8).getSeconds()));

                try {

                    url = new URI("http://dummy" + uriStr).getPath();
                    url = url.startsWith("//") ? url.substring(2) : url;

                } catch (Exception e) {

                    System.out.println("new uri failed, please check");

                }

            } else {

                System.out.println("Log format does not match expected pattern");

            }

            Tuple2<Long, String> tuple2 = new Tuple2<Long, String>();
            tuple2.setFields(timestamp, url);

            return tuple2;

        }
    }
}
