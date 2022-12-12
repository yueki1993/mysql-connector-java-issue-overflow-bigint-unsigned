# mysql-connector-java-issue-overflow-bigint

I suspect mysql-connector-java>=8.0.22 has an overflow bug for values `bignum.signed.max+1 <= values <= bignum.unsigned.max`.  
For a table like
```sql
create table tbl(
  k int,
  v bigint unsigned
);
```
we get no error when inserting values using mysql client:
```sql
mysql> insert into tbl values(1,18446744073709551615);
Query OK, 1 row affected (0.00 sec)

mysql> select * from tbl where k = 1;
+------+----------------------+
| k    | v                    |
+------+----------------------+
|    1 | 18446744073709551615 |
+------+----------------------+
1 row in set (0.00 sec)
```

We can insert values without error using mysql-client-java==8.0.21, 
whereas version>=8.0.22 will raise `com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Out of range value for column 'v' at row 1`.

I suspect [NumberValueEncoder](https://github.com/mysql/mysql-connector-j/blob/8.0.31/src/main/protocol-impl/java/com/mysql/cj/protocol/a/NumberValueEncoder.java#L68)
has a bug because:
```java
    BigInteger i = new BigInteger("18446744073709551615");
    assertThat(String.valueOf(i.longValue())).isEqualTo("-1"); // Long.MAX_VALUE: 9223372036854775807, which is smaller than `i`.
```

## how to run poc
Run `docker compose up --build` and  `InsertBigIntegerTest`.

