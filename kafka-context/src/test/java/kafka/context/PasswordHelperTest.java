package kafka.context;

import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasswordHelperTest {

  @Test
  void shouldMatch() throws NoSuchAlgorithmException {
    var salt = PasswordHelper.generateKey();
    System.out.println(salt);
    var h = new PasswordHelper(salt);
    var strToEncrypt = "test/123/test";
    var enc = h.encrypt(strToEncrypt);
    var dec = h.decrypt(enc);
    System.out.println(dec);
    Assertions.assertEquals(strToEncrypt, dec);
  }
}
