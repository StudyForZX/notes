# 数据页

## 1. 组成

1. `File Header`（文件头）
2. `Page Header`（页头）
3. `Infimun` 和 `Supermum Records`
4. `User Records` (用户行为，即行记录)
5. `Free Space`（空闲空间）
6. `Page Directory`（页目录）
7. `File Trailer` (文件结尾信息)

其中`File Header`，`Page Header`, `File Trailer`的大小是固定的，分别为`38`，`56`，`8`字节，这些空间用来标记该页的一些信息，如`checksum`，数据页所在`B+`树索引的层数等。`User Records`、`Free Space`、`Page Directory`这部分为实际的行记录存储空间，因此大小是动态的，如下图：

![idb引擎数据页结构](https://github.com/StudyForZX/notes/blob/master/mysql/images/page_size_struct_idb.png)

### 1.1 File Header

`File Header`用来记录页的一些头信息，由表中的8个部分组成，共占用38字节。

| 名称 | 大小（字节）| 说明 |
| :-: | :-: | :-: |
| FIL_PAGE_SPACE_OR_CHKSUM | 4 | 当MySQL为MySQL4.0.14之前的版本时，该值为0。之后的版本代表页的checksum值。|
| FIL_PAGE_OFFSET | 4 | 表空间中页的偏移值。如某独立表空间`a.ibd`的大小为1GB，如果页的大小为`16KB`，那么总共有`65536`个页。该参数表示该页在所有页中的位置。若此表空间的ID为10，那么搜索页（10，1）就表示查找表a中的第二个页。|
| FILE_PAGE_PREV | 4 | 当期页的上一个页，B+Tree特性决定了叶子节点必须是双向列表。|
| FILE_PAGE_NEXT | 4 | 当期页的下一个页，B+Tree特性决定了叶子节点必须是双向列表。|
| FIL_PAGE_LSN | 8 | 该值代表该页最后被修改的日志序列位置LSN（Log Sequence Number）|
| FIL_PAGE_TYPE | 2 | InnoDB存储引擎页的类型。常见的类型见下图。记住0x45BF，该值代表了存放的数据页，即实际行记录的存储空间。|
| FIL_PAGE_FILE_FLUSH_LSN | 8 | 该值仅在系统表空间的一个页中定义，代表文件至少被更新到了该LSN值。对于独立表空间，该值都为0。|
| FIL_PAGE_ARCH_LOG_NO_OR_SPACE_ID | 8 | 从MySQL4.1开始，该值代表属于哪个表空间。|

InnoDB存储引擎中页的类型

| 名称 | 十六进制 | 解释 |
| :-: | :-: | :-: |
| FIL_PAGE_INDEX | 0x45BF | B+树叶节点 |
| FIL_PAGE_UNDO_LOG | 0x0002 | Undo Log页 |
| FIL_PAGE_INODE | 0x0003 | 索引节点 |
| FIL_PAGE_IBUF_FREE_LIST | 0x0004 | Insert Buffer空闲列表 |
| FIL_PAGE_TYPE_ALLOCATED | 0x0000 | 该页为最新分配 |
| FIL_PAGE_IBUF_BITMAP | 0x0005 | Insert Buffer位图 |
| FIL_PAGE_TYPE_SYS | 0x0006 | 系统页 |
| FIL_PAGE_TYPE_TRX_SYS | 0x0007 | 事务系统数据 |
| FIL_PAGE_TYPE_FSP_HDR | 0x0008 | File Space Header |
| FIL_PAGE_TYPE_XDES | 0x0009 | 扩展描述页 |
| FIL_PAGE_TYPE_BLOB | 0x000A | BLOB页 |

### 1.2 Page Head

接着`File Header`部分的是`Page Header`，该部分用来记录数据页的状态信息，由14个部分组成，共占用56字节，如下图表：

| 名称 | 大小（字节）| 说明 |
| PAGE_N_DIR_SLOTS | 2 | 在页目录中的slot（槽）数 |
| PAGE_HEAP_TOP | 2 | 堆中第一个记录的指针，记录在页中是根据堆的形式存放的 |
| PAGE_N_HEAP | 2 | 堆中的记录数。第15位表示行记录格式 |
| PAGE_FREE | 2 | 指向可重用空间的首指针 |
| PAGE_GARBAGE | 2 | 已删除记录的字节数，即行记录结构中`delete flag`为1的记录大小的总数 |
| PAGE_LAST_INSERT | 2 | 最后插入记录的位置 |
| PAGE_DIRECTION | 2 | 最后插入的方向。可能的取值：`PAGE_LEFT(0x01)`、`PAGE_RIGHT(0x02)`、`PAGE_SAME_REC(0x03)`、`PAGE_SAME_PAGE(0x04)`、`PAGE_NO_DIRECTION(0x05)`|
| PAGE_N_DIRECTION | 2 | 一个方向连续插入记录的数量 |
| PAGE_N_RECS | 2 | 该页中记录的数量 |
| PAGE_MAX_TRX_ID | 8 | 修改当前页的最大事务ID，注意该值仅在`Secondary Index`中定义 |
| PAGE_LEVEL | 2 | 当前页在索引中的位置，`0x00`代表叶子节点，即叶节点总是在第0层 |
| PAGE_INDEX_ID | 8 | 索引ID，表示当前页属于哪个索引 |
| PAGE_BTR_SEG_LEAF | 10 | B+数据页非叶节点所在段的`segment header`。注意该值仅在B+树的Root页中定义 |
| PAGE_BTR_SEG_TOP | 10 | B+数据页所在段的`segment header`。注意该值仅在B+树的Root页中定义 |

## 2. 虚拟行记录 Infimum 和 Supremum Record

每个数据页中有两个虚拟的行记录，用来限定记录的边界。

`Infimum`记录是比该页中任何主键值都要小的值，`Supremum`指比任何可能大的值还要大的值。这两个值在页创建时被建立，并且在任何情况下不会被删除。

如下图：![`Infimum`与`Supremum Record`](https://github.com/StudyForZX/notes/blob/master/mysql/images/infimum_and_supremum.png)

## 3. User Record 和 Free Space

`User Record`就是实际存储行记录的内容，InnoDB存储引擎表总是B+树索引组织的。
`Free Space`指的是空闲空间，同样也是个链表数据结构。在一条记录被删除后，该空间会被加入到空闲链表中。

## 4. Page Direction

`Page Direction`（页目录）中存放了页的相对位置（不是偏移量），也会被称为`Slots`或`Direction Slots`。

InnoDB不是每个记录拥有一个`slot`，`slot`是一个`稀疏目录`（`sparse direction`），即一个`slot`可能包含多个记录。伪记录`Infimum`的`n_owned`值总是1，记录`Supremum`的`n_owned`的取值范围为[1, 8]，其他用户记录`n_onwed`的取值范围是[4, 8]。当记录被插入或删除时需要对`slot`进行分裂或平衡的维护操作。

由于`Page Direction`是一个稀疏目录，二叉查找的结果只是一个粗略的结果，因此InnoDB必须通过`recorder header`中的`next_record`来继续查找相关记录。

`B+`树索引本身并不能找到具体的一条记录，能找到只是该记录所在的页。
