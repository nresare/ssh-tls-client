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

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses single line OpenSSH RSA public keys.
 *
 * Copied slightly modified from TraditionalKeyParser from https://github.com/spotify/crtauth-java
 */
public class SSHKeyParser {

  static final KeyFactory RSA_KEY_FACTORY;
  static {
    try {
      RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Can't understand RSA keys, your env is broken", e);
    }
  }

  private static final Pattern PUBLIC_KEY_PATTERN = Pattern.compile(
      "^ssh-rsa (.+)[ \n].*$", Pattern.MULTILINE);
  private static final String PUBLIC_KEY_TYPE = "ssh-rsa";

  /**
   * Parse an ssh public RSA key encoded in the format emitted by the OpenSSH
   * ssh-keygen tool.
   *
   * @param key a public key string as found in id_rsa.pub
   * @return an RSAPublicKeySpec
   * @throws InvalidKeyException if the provided key contain an invalid key
   */
  public static PublicKey parseOpenSSHPubKey(final String key) throws InvalidKeyException {
    Matcher matcher = PUBLIC_KEY_PATTERN.matcher(key);
    if (!matcher.matches()) {
      throw new InvalidKeyException("key '" + key + "' is of wrong format");
    }
    final String keyPart = matcher.group(1);
    BaseEncoding encoding = BaseEncoding.base64();
    byte[] derKey = encoding.decode(keyPart);
    final ByteBuffer byteBuffer = ByteBuffer.wrap(derKey);
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    byte[] typeBytes = readVariableLengthOpaque(byteBuffer);
    byte[] expBytes = readVariableLengthOpaque(byteBuffer);
    byte[] modBytes = readVariableLengthOpaque(byteBuffer);
    if (typeBytes == null || expBytes == null || modBytes == null) {
      throw new InvalidKeyException();
    }
    final String type = new String(typeBytes, Charsets.US_ASCII);
    if (!type.equals(PUBLIC_KEY_TYPE)) {
      throw new InvalidKeyException("Wrong public key type: " + type);
    }
    final BigInteger exp = new BigInteger(expBytes);
    final BigInteger mod = new BigInteger(modBytes);
    try {
      return RSA_KEY_FACTORY.generatePublic(new RSAPublicKeySpec(mod, exp));
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }

  }

  private static byte[] readVariableLengthOpaque(final ByteBuffer byteBuffer) {
    if (byteBuffer.position() + Integer.SIZE > byteBuffer.limit()) {
      return null;
    }
    int length = byteBuffer.getInt();
    if (byteBuffer.position() + length > byteBuffer.limit()) {
      return null;
    }
    byte[] bytes = new byte[length];
    byteBuffer.get(bytes);
    return bytes;
  }

}
