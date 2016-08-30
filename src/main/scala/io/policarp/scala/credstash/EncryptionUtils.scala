package io.policarp.scala.credstash

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.Hex

object EncryptionUtils {

  implicit class ToHexDigest(bytes: Array[Byte]) {
    def toHexDigest = new String(Hex.encodeHex(bytes))
  }

  implicit class FromHexDigest(chars: Array[Char]) {
    def fromHexDigest = Hex.decodeHex(chars)
  }

  /**
   * ref: http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
   */
  object HmacSHA256 {
    @throws[Exception]
    def apply(data: Array[Byte], key: Array[Byte]) = {
      val algorithm = "HmacSHA256"
      val mac = Mac.getInstance(algorithm)
      mac.init(new SecretKeySpec(key, algorithm))
      mac.doFinal(data)
    }
  }

}