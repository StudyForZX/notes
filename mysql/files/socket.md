# 套接字文件

## 概念

Unix系统下连接本地MySQL可以采用UNIX域套接字方式，这种方式需要一个套接字文件。

## 参数

### socket

一般在/tmp目录下，名为mysql.sock

```mysql
SHOW VARIABLES LIKE 'socket'\G;
```

## 作用
