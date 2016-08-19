package au.com.simplemachines.scala.credstash

import java.nio.ByteBuffer
import java.nio.charset.Charset

import au.com.simplemachines.scala.credstash.reader.CredValueReader
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, ComparisonOperator, Condition, QueryRequest }
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.util.Base64

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

object SimpleCredStashClient {
  def apply(kms: AWSKMSClient, dynamo: AmazonDynamoDBClient, aes: AESEncryption = DefaultAESEncryption) = {
    new SimpleCredStashClient {
      override val kmsClient = kms
      override val dynamoClient = dynamo
      override val aesEncryption = aes
    }
  }
}

trait SimpleCredStashClient extends BaseClient with AmazonClients with EncryptionClients {

  import BaseClient._

  override type KmsClient = AWSKMSClient
  override type DynamoClient = AmazonDynamoDBClient

  override def get[K](name: String, table: String = DefaultCredentialTableName, version: String = "-1", context: EncryptionContext = EmptyEncryptionContext)(implicit reader: CredValueReader[K]): Option[K] = {
    val credStashItem = version match {
      case "-1" => getMostRecentValue(name, table)
      case _ => getVersionedValue(name, table, version)
    }
    credStashItem.fold[Option[K]](None)(material => decryptItem(material, context))
  }

  private def getMostRecentValue[K](name: String, table: String): Option[CredStashMaterial] = {

    val keyCondition = new Condition()
      .withComparisonOperator(ComparisonOperator.EQ)
      .withAttributeValueList(List(new AttributeValue(name)).asJavaCollection)

    val queryRequest = new QueryRequest(table)
      .withLimit(1)
      .withScanIndexForward(false)
      .withConsistentRead(true)
      .addKeyConditionsEntry("name", keyCondition)

    Try(dynamoClient.query(queryRequest)) match {
      case Success(result) =>
        if (result.getCount != 0) {
          Some(CredStashMaterial(result.getItems.get(0)))
        } else {
          None
        }
      case Failure(e) =>
        None // TODO report issue
    }
  }

  private def getVersionedValue[K](name: String, table: String, version: String): Option[CredStashMaterial] = {
    Try(dynamoClient.getItem(table, Map("name" -> new AttributeValue(name), "version" -> new AttributeValue(version.toString)).asJava)) match {
      case Success(result) =>
        Option(result.getItem).fold[Option[CredStashMaterial]](None)(item => Some(CredStashMaterial(item)))
      case Failure(e) =>
        None // TODO report issue
    }
  }

  private def decryptItem[K](credStashMaterial: CredStashMaterial, context: EncryptionContext)(implicit reader: CredValueReader[K]): Option[K] = {

    import EncryptionUtils._
    import BaseClient._

    val checkKeyRequest = new DecryptRequest()
      .withCiphertextBlob(ByteBuffer.wrap(Base64.decode(credStashMaterial.key)))
      .withEncryptionContext(context.asJava)

    Try(kmsClient.decrypt(checkKeyRequest)) match {
      case Failure(e) =>
        None // TODO report issue

      case Success(result) =>
        val plainTextArray = result.getPlaintext.array()
        val key = plainTextArray.take(32)
        val hmacKey = plainTextArray.takeRight(32)

        val unencodedContents = Base64.decode(credStashMaterial.contents)
        val hmac = HmacSHA256(unencodedContents, hmacKey)

        if (hmac.toHexDigest == credStashMaterial.hmac) {
          val decryptedString = new String(aesEncryption.decrypt(key, unencodedContents), Charset.forName(DefaultCharacterEncoding))
          Some(reader.read(decryptedString))
        } else {
          None // TODO report issue
        }
    }
  }
}
