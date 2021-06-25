# 错误日志

## 概念

错误日志对MySQL的启动、运行、关闭过程进行了记录。

```mysql
-- 定位该文件
SHOW VARIABLES LIKE 'log_error'
```

默认情况下，错误文件的文件名为服务器的主机名。
