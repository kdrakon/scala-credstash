package au.com.simplemachines.scala.credstash

import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

trait AESEncryption {
  def decrypt(key: Array[Byte], encryptedValue: Array[Byte]): Array[Byte]
}

/**
  * ref: https://gist.github.com/alexandru/ac1c01168710786b54b0
  */
object DefaultAESEncryption extends AESEncryption {

  private def cipher(key: Array[Byte], encryptMode: Int) = {
    val cipher = Cipher.getInstance("AES/CTR/NoPadding")

    // based on Python's Crypto.Cipher.AES and Crypto.Util.Counter
    val blockSize = cipher.getBlockSize
    // ref: https://pythonhosted.org/pycrypto/Crypto.Util.Counter-module.html
    // Python default is Big Endian
    val counter = Array.fill[Byte](blockSize - 1)(0) ++ Array[Byte](1)
    val ivParameterSpec = new IvParameterSpec(counter)

    cipher.init(encryptMode, keyToSpec(key), ivParameterSpec)
    cipher
  }

  def keyToSpec(key: Array[Byte]): SecretKeySpec = new SecretKeySpec(key, "AES")

  def encrypt(key: Array[Byte], value: Array[Byte]): Array[Byte] = {
    cipher(key, Cipher.ENCRYPT_MODE).doFinal(value)
  }

  def decrypt(key: Array[Byte], encryptedValue: Array[Byte]): Array[Byte] = {
    cipher(key, Cipher.DECRYPT_MODE).doFinal(encryptedValue)
  }
}
