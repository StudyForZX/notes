package org.hzx;

// import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

public class KafkaSourceDemo {

    public static String userName = "read_pay4_6";
    public static String password = "f912c965c3b6aab0230c49703366bf26";

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // env.enableCheckpointing(10000);

        // 强烈建议将 Kafka 的事务超时时间调整至远大于 checkpoint 最大间隔 + 最大重启时间，否则 Kafka 对未提交事务的过期处理会导致数据丢失。
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                    .setBootstrapServers("10.107.36.95:9082,10.107.36.98:9082,10.107.36.99:9082")
                    .setGroupId("hawking-group-100101-test")
                    .setTopics("hawking.100101.pay4.az1.origin")
                    .setStartingOffsets(OffsetsInitializer.earliest())
                    .setValueOnlyDeserializer(new SimpleStringSchema())
                    // .setProperty("enable.auto.commit", "false") // 关闭自动提交,避免与`checkpoint`机制冲突
                    // .setProperty("isolation.level", "read_committed")
                    .setProperty("partition.discovery.interval.ms", "10000") // 定期检查新分区 ms
                    .setProperty("security.protocol", "SASL_PLAINTEXT")
                    .setProperty("sasl.mechanism", "PLAIN")
                    .setProperty("sasl.jaas.config", String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", userName, password))
                    .build();

        DataStreamSource<String> kafkaDS = env.fromSource(
            kafkaSource,
            WatermarkStrategy.noWatermarks(),
            "kafka-source"
        );

        kafkaDS.print();

        // kafkaDS.map(new kafkaMessageMap());

        env.execute();
    }

    // public static class kafkaMessageMap implements MapFunction<String, String> {

    //     public String map(String value) {

    //         return value;
    //     }
    // }

}
