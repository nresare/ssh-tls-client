/*
 * Copyright (c) 2015 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spotify.sshtls.validator;

import com.google.common.io.BaseEncoding;
import org.junit.Test;
import sun.security.pkcs.PKCS8Key;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Tests SSHKeyParser
 */
public class SSHKeyParserTest {
  private static final String PKCS1_PEM_PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDjKPW" +
      "24a0Go7ETiMP/j8DsnG+E6bB2DvCuX5hJLbBKE/oBMsZ3eB8MyROXmv/h0b0OzugEx+llxIo0FpnsuJxMlF7xpEp7d" +
      "HKHTUdxWIclmGjI6tzurX+sDerUuJk9gNj3SK67lcZI5tsrXjDsy+ZpVQWcL/6trB9r69VDGm+GfnC8JIItLesAbJ1" +
      "IcSq4/oU3e0mRjiaf5X/bMy1lRejcqEOARWhTVTw3D+EdPqAWZPh1IzREPnoNVp5MeSVU4hRdoZmJPwP9qF4f2qbhs" +
      "w0cDDPNFigU/UDw2kW9CUlGscrPs+0sj9wim4ZwMC9hmiFS/yfzHOaoTylFkG6ia9W/ test@spotify.net";

  private static final String X509_PEM_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA" +
      "4yj1tuGtBqOxE4jD/4/A7JxvhOmwdg7wrl+YSS2wShP6ATLGd3gfDMkTl5r/4dG9Ds7oBMfpZcSKNBaZ7LicTJRe8a" +
      "RKe3Ryh01HcViHJZhoyOrc7q1/rA3q1LiZPYDY90iuu5XGSObbK14w7MvmaVUFnC/+rawfa+vVQxpvhn5wvCSCLS3r" +
      "AGydSHEquP6FN3tJkY4mn+V/2zMtZUXo3KhDgEVoU1U8Nw/hHT6gFmT4dSM0RD56DVaeTHklVOIUXaGZiT8D/aheH9" +
      "qm4bMNHAwzzRYoFP1A8NpFvQlJRrHKz7PtLI/cIpuGcDAvYZohUv8n8xzmqE8pRZBuomvVvwIDAQAB";

  @Test
  public void testDecodePublicKey() throws Exception {
    final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    BaseEncoding encoding = BaseEncoding.base64();
    byte[] expectedKeyBytes = encoding.decode(X509_PEM_PUBLIC_KEY);
    KeySpec publicKeySpec = new X509EncodedKeySpec(expectedKeyBytes);
    PublicKey expected = keyFactory.generatePublic(publicKeySpec);
    PublicKey actual = SSHKeyParser.parseOpenSSHPubKey(PKCS1_PEM_PUBLIC_KEY);
    assertEquals(expected, actual);
  }

  private static final Pattern PRIVATE_KEY_PATTERN =
      Pattern.compile("^-+BEGIN RSA PRIVATE KEY-+([^-]+)-+END RSA PRIVATE KEY[-\n]+$");
  //Pattern.compile(".*(R.A).*", Pattern.DOTALL);

  @Test
  public void noaPlayTest() throws Exception {
    String s = new String(Files.readAllBytes(Paths.get("/Users/noa/slask/cert/localhost.key")));

    Matcher m = PRIVATE_KEY_PATTERN.matcher(s);
    if (!m.matches()) {
      throw new RuntimeException("Invalid cert format: "+ s);
    }


    System.out.println(new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(m.group(1))));
  }

}
