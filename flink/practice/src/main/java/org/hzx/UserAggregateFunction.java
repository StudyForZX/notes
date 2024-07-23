package org.hzx;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.AggregateFunction;

public class UserAggregateFunction {
    /**
     * 自定义聚合方法
     */
    private static class CountApiNo implements AggregateFunction<Tuple2<Long, String>, Tuple2<String, Integer>, Integer>{

        @Override
        public Tuple2<String, Integer> createAccumulator() {

            System.out.println("createAccumulator");

            return new Tuple2<>("", 0);
        }

        @Override
        public Tuple2<String, Integer> add(Tuple2<Long, String> value, Tuple2<String, Integer> accumulator) {

            System.out.println("add func: value is: " + value + " accumulator is: " + accumulator);

            return new Tuple2<>(accumulator.f0, accumulator.f1 + 1);
        }

        @Override
        public Integer getResult(Tuple2<String, Integer> accumulator) {

            System.out.println("getResult func: accumulator is: " + accumulator);

            return accumulator.f1;
        }

        @Override
        public Tuple2<String, Integer> merge(Tuple2<String, Integer> a, Tuple2<String, Integer> b) {

            System.out.println("merge func: a is: " + a + " b is: " + b);

            return new Tuple2<>(a.f0, a.f1 + b.f1);
        }

    }


}

