# 表结构文件

## frm文件

不论采用哪种存储引擎，MySQL都有一个以frm为后缀名的文。

### 作用

1. 记录表结构定义。
2. 存放视图的定义。如果创建了一个v_a视图，那么对应地会产生一个v_a.frm文件，用来记录视图的定义。

## InnoDB存储引擎

InnoDB采用将存储的数据按表空间进行存放的设计。默认配置下会有一个初始大小为10M的ibdata1文件，该文件就是默认的表空间文件。

用户可以通过参数`innodb_data_file_path`对其进行配置，格式如下

```config
innodb_data_file_path=datafile_spec1[;datafile_spec2]

# 用户可以通过多个文件组成一个表空间，同时制定文件的属性，如：
[mysqld]
innodb_data_file_path=/db/ibdata1:2000M;/dr2/db/ibdata2:2000M:autuextend

# 两个文件位于不同的磁盘上，磁盘的负载可能被平均，因此可以提高数据库的整体性能。

```

### 参数

1. innodb_data_file_path  控制全局存储
2. innodb_file_per_table  控制单表
