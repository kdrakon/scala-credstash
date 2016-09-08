package io.policarp.scala.credstash

import java.nio.ByteBuffer
import java.nio.charset.Charset

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, ComparisonOperator, Condition, QueryRequest }
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.util.Base64
import io.policarp.scala.credstash.reader.CredValueReader

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

  private[credstash] def constructNameQuery(name: String, table: String): QueryRequest = {
    val keyCondition = new Condition()
      .withComparisonOperator(ComparisonOperator.EQ)
      .withAttributeValueList(List(new AttributeValue(name)).asJavaCollection)

    new QueryRequest(table)
      .withLimit(1)
      .withScanIndexForward(false)
      .withConsistentRead(true)
      .addKeyConditionsEntry("name", keyCondition)
  }

  private[credstash] def versionRequestMap(name: String, version: String) =
    Map("name" -> new AttributeValue(name), "version" -> new AttributeValue(version.toString)).asJava
}

trait SimpleCredStashClient extends BaseClient with AmazonClients with EncryptionClients {

  import BaseClient._
  import SimpleCredStashClient._

  override type KmsClient = AWSKMSClient
  override type DynamoClient = AmazonDynamoDBClient

  override def as[K](name: String, table: String = DefaultCredentialTableName, version: String = MostRecentVersion, context: Map[String, String] = EmptyEncryptionContext)(implicit reader: CredValueReader[K]): Option[K] = {
    val credStashItem = version match {
      case MostRecentVersion => getMostRecentValue(name, table)
      case someVersion => getVersionedValue(name, table, someVersion)
    }
    credStashItem.fold[Option[K]](None)(material => decryptItem(material, context))
  }

  private def getMostRecentValue[K](name: String, table: String): Option[CredStashMaterial] = {

    val queryRequest = constructNameQuery(name, table)

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
    Try(dynamoClient.getItem(table, versionRequestMap(name, version))) match {
      case Success(result) =>
        Option(result.getItem).fold[Option[CredStashMaterial]](None)(item => item.isEmpty match {
          case false => Some(CredStashMaterial(item))
          case true => None
        })
      case Failure(e) =>
        None // TODO report issue
    }
  }

  private def decryptItem[K](credStashMaterial: CredStashMaterial, context: EncryptionContext)(implicit reader: CredValueReader[K]): Option[K] = {

    import EncryptionUtils._

    val decryptKeyRequest = new DecryptRequest()
      .withCiphertextBlob(ByteBuffer.wrap(Base64.decode(credStashMaterial.key)))
      .withEncryptionContext(context.asJava)

    Try(kmsClient.decrypt(decryptKeyRequest)) match {
      case Failure(e) =>
        None // TODO report issue

      case Success(result) =>
        val plainTextArray = result.getPlaintext.array()
        val aesKey = plainTextArray.take(32)
        val hmacKey = plainTextArray.takeRight(32)

        val unencodedContents = Base64.decode(credStashMaterial.contents)
        val hmac = HmacSHA256(unencodedContents, hmacKey)

        if (hmac.toHexDigest == credStashMaterial.hmac) {
          val decryptedString = new String(aesEncryption.decrypt(aesKey, unencodedContents), Charset.forName(DefaultCharacterEncoding))
          Some(reader.read(decryptedString))
        } else {
          None // TODO report issue
        }
    }
  }
}
