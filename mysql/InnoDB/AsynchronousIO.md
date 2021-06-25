# 异步IO

## 概念

InnoDB采用异步IO方式处理磁盘操作。

## 刷新相邻页

InnoDB存储引擎提供了Flush Neighbor Page（刷新相邻页）的特性。其工作原理为：当刷新一个脏页时，InnoDB存储引擎会检测该页所在区的所有页，如果是脏页，那么一起合并刷新，通过AIO可以将多个IO写入操作合并到一个IO操作。

如果是固态硬盘有着超高IOPS性能的磁盘，建议关闭此功能（innodb_flush_neighbors）。
