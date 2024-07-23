package org.hzx;

import org.apache.flink.util.Collector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;

public class MyProcessWindowFunction {

    public static class MyProcessWindowFunction extends ProcessWindowFunction<Tuple2<Long, String>, String, String, TimeWindow> {

        @Override
        public void process(
            String key, // 分组的key
            ProcessWindowFunction<Tuple2<Long, String>, String, String, TimeWindow>.Context ctx, // 时间窗口上下文
            Iterable<Tuple2<Long, String>> elements, // 存的数据集
            Collector<String> out // 采集器
        ) throws Exception {

            long tsStart = ctx.window().getStart();
            long tsEnd = ctx.window().getEnd();
            String windowStart = DateFormatUtils.format(tsStart, "yyyy-MM-dd HH:mm:ss.SSS");
            String windowEnd = DateFormatUtils.format(tsEnd, "yyyy-MM-dd HH:mm:ss.SSS");

            long count = elements.spliterator().estimateSize();

            String resStr = "key=[" + key + "]的窗口[" + windowStart + ", "+ windowEnd + ")包含" + count + "条数据===>" + elements.toString();

            out.collect(resStr);
        }
    }


}

