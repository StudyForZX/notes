# slice - 切片

A slice is a **dynamically-sized**, flexible view into the elements of an array

划重点：`动态大小`

## 定义

The type []T is a slice with elements of type T.

A slice is formed by specifying two indices, a low and high bound, separated by a colon:

`a[low : high]`

This selects a half-open range which includes the first element, but excludes the last one.

> `[0:len(slice)]`简写为`[:]`

## 样例解析

```go
primes := [6]int{2, 3, 5, 7, 11, 13}

var s []int = primes[:]

fmt.Println(s, len(s), cap(s))

// 输出 [2, 3, 5, 7, 11, 13] len=6 cap=6

s1 := primes[:4]

fmt.Println(s1, len(s1), cap(s1))

// 输出 [2, 3, 5, 7] len=4 len=6 哦 这里len不变啊

s2 := primes[1:4]

fmt.Println(s2, len(s2), cap(s2))

// 输出 [3 5 7] len=3 len=5 等等这里len怎么变了？？？

fmt.Printf("%p\n", s)  // 0xc00011e030
fmt.Printf("%p\n", s1) // 0xc00011e030
fmt.Printf("%p\n", s2) // 0xc00011e038

// 从前切割数据导致开辟了新的存储空间，具体原因暂未查

```
