# 慢查询日志

## 参数

### long_query_time

默认：10s，并且默认不启动，需要手动设置为`ON`。

```mysql
SHOW VARIABLES LIKE 'long_query_time'\G; -- 时间
SHOW VARIABLES LIKE 'long_query_queries'\G; -- 开关
```

> 源代码判断的是大于long_query_time，而非大于等于。MySQL5.1开始使用微妙记录。

### long_queries_not_using_indexes

打开此配置后，如果查询语句没有使用索引，则会将其记录到慢查询日志中。

```mysql
SHOW VARIABLES LIKE 'log_queries_not_using_indexes'\G; -- 开关
```

### long_throttle_queries_not_using_indexes

表示每分钟允许记录到慢日志且未使用索引的SQL语句次数。默认为0，表示没有限制。
