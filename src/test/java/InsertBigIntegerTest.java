import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InsertBigIntegerTest {

  private static Connection conn;

  @BeforeAll
  static void beforeAll() throws Exception {
    conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/db?user=root&password=r00tr00t");
    conn.setAutoCommit(true);
  }

  @BeforeEach
  void setUp() throws Exception {
    conn.createStatement().executeUpdate("delete from tbl");
  }

  @Test
  void test_0() throws Exception {
    insertAndAssert(0, new BigInteger("0"));
  }

  @Test
  void test_1() throws Exception {
    insertAndAssert(1, new BigInteger("1"));
  }

  @Test
  void test_bigint_signed_max() throws Exception {
    // https://dev.mysql.com/doc/refman/8.0/en/integer-types.html
    // 2^63-1
    BigInteger val = new BigInteger("2").pow(63).subtract(new BigInteger("1"));
    assertThat(val.toString()).isEqualTo("9223372036854775807");
    insertAndAssert(2, val);
  }

  // bug?
  @Test
  void test_bigint_signed_max_plus_1() throws Exception {
    // https://dev.mysql.com/doc/refman/8.0/en/integer-types.html
    // 2^63
    BigInteger val = new BigInteger("2").pow(63);
    assertThat(val.toString()).isEqualTo("9223372036854775808");
    // mysql client: no error
    //  mysql> insert into tbl values(3,9223372036854775808);
    //  Query OK, 1 row affected (0.00 sec)
    // mysql-connector-java: error
    //  com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Out of range value for column 'v' at row 1
    insertAndAssert(3, val);
  }

  // bug?
  @Test
  void test_bigint_unsigned_max() throws Exception {
    // https://dev.mysql.com/doc/refman/8.0/en/integer-types.html
    // 2^64-1
    BigInteger val = new BigInteger("2").pow(64).subtract(new BigInteger("1"));
    assertThat(val.toString()).isEqualTo("18446744073709551615");

    // mysql client: no error
    //  mysql> insert into tbl values(3,4,18446744073709551615);
    //  Query OK, 1 row affected (0.00 sec)
    // mysql-connector-java: error
    //  com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Out of range value for column 'v' at row 1
    insertAndAssert(4, val);
  }


  // workaround
  @Test
  void test_bigint_unsigned_max_string() throws Exception {
    // https://dev.mysql.com/doc/refman/8.0/en/integer-types.html
    // 2^64-1
    BigInteger val = new BigInteger("2").pow(64).subtract(new BigInteger("1"));
    assertThat(val.toString()).isEqualTo("18446744073709551615");
    insertAndAssert(5, val.toString()); // no error
  }


  void insertAndAssert(int k, Object v) throws Exception {
    // insert
    {
      String sql = "insert into tbl values (?,?)";

      PreparedStatement ps = conn.prepareStatement(sql);

      ps.setObject(1, k);
      ps.setObject(2, v);

      ps.executeUpdate();
    }

    // assert
    {
      String sql = "select * from tbl where k = ?";

      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setObject(1, k);
      ResultSet rs = ps.executeQuery();
      rs.next();
      int kAct = rs.getInt("k");
      String vAct = rs.getString("v");

      assertThat(kAct).isEqualTo(k);
      assertThat(vAct).isEqualTo(v.toString());
    }
  }
}
