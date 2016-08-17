package au.com.simplemachines.scala.credstash

import java.nio.ByteBuffer
import java.nio.charset.Charset

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition, QueryRequest}
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.util.Base64

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait SimpleCredStashClient extends BaseClient {

  override type KmsClient = AWSKMSClient
  override type DynamoClient = AmazonDynamoDBClient

  override def get[K](name: String, table: String = BaseClient.defaultCredentialTableName, version: String = "-1")(implicit reader: CredValueReader[K]): Option[K] = {
    val credStashItem = version match {
      case "-1" => getMostRecentValue(name, table)
      case _ => getVersionedValue(name, table, version)
    }
    credStashItem.fold[Option[K]](None)(c => decryptItem(c))
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

  private def decryptItem[K](credStashMaterial: CredStashMaterial)(implicit reader: CredValueReader[K]): Option[K] = {

    import EncryptionUtils._

    val checkKeyRequest = new DecryptRequest()
      .withCiphertextBlob(ByteBuffer.wrap(Base64.decode(credStashMaterial.key)))
      .withEncryptionContext(BaseClient.EmptyEncryptionContext) // TODO implement support for these contexts

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
          val decryptedString = new String(AESEncryption.decrypt(key, unencodedContents), Charset.forName("UTF8"))
          Some(reader.read(decryptedString))
        } else {
          None // TODO report issue
        }
    }
  }
}
