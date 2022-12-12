import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class NumberValueEncoderTest {

  @Test
  void suspicious_logic() {
    /**
     * suspecious logic:
     * https://github.com/mysql/mysql-connector-j/blob/release/8.0/src/main/protocol-impl/java/com/mysql/cj/protocol/a/NumberValueEncoder.java#L66-L68
     *             case BIGINT:
     *             case BIGINT_UNSIGNED:
     *                 return String.valueOf(x.longValue());
     *
     */
    BigInteger i1 = new BigInteger("9223372036854775807");
    assertThat(String.valueOf(i1.longValue())).isEqualTo("9223372036854775807");

    BigInteger i2 = new BigInteger("9223372036854775808");
    assertThat(String.valueOf(i2.longValue())).isEqualTo("-9223372036854775808"); // bug?

    BigInteger i3 = new BigInteger("18446744073709551615");
    assertThat(String.valueOf(i3.longValue())).isEqualTo("-1"); // bug?
  }
}
