package au.com.simplemachines.scala.credstash

import java.nio.ByteBuffer

import au.com.simplemachines.scala.credstash.TestUtils._
import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, GetItemResult, QueryResult }
import com.amazonaws.services.kms.model.{ DecryptRequest, DecryptResult }
import com.amazonaws.util.Base64
import org.mockito.Matchers.{ any => mockitoAny }
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{ Matchers, WordSpecLike }

import scala.collection.JavaConverters._

class SimpleCredStashClientTest extends SimpleCredStashClientTestHarness with WordSpecLike with Matchers {

  "SimpleCredStashClientTest and Dynamo" should {
    "return None when there exists no value in Dynamo" in {

      when(dynamoClient.query(mockitoAny())).thenReturn(
        new QueryResult() {
          this.setCount(0)
        }
      )

      newClient.get("password") shouldBe None
      verify(dynamoClient, times(1)).query(SimpleCredStashClient.constructNameQuery("password", BaseClient.DefaultCredentialTableName))
    }

    "return None when Dynamo fails" in {

      when(dynamoClient.query(mockitoAny())).thenAnswer(new Answer[QueryResult]() {
        override def answer(invocation: InvocationOnMock) = throw new RuntimeException("oops")
      })

      newClient.get("password") shouldBe None
      verify(dynamoClient, times(1)).query(SimpleCredStashClient.constructNameQuery("password", BaseClient.DefaultCredentialTableName))
    }

    "return None when a version does not exist" in {

      val version = "123"
      val getItemResult = new GetItemResult()
      when(dynamoClient.getItem(mockitoAny(), mockitoAny())).thenReturn(
        getItemResult
      )

      getItemResult.setItem(null)
      newClient.get("password", version = version) shouldBe None

      getItemResult.setItem(Map[String, AttributeValue]().asJava)
      newClient.get("password", version = version) shouldBe None

      when(dynamoClient.getItem(mockitoAny(), mockitoAny())).thenAnswer(
        new Answer[GetItemResult]() {
          override def answer(invocation: InvocationOnMock) = throw new RuntimeException("oops")
        }
      )
      newClient.get("password", version = version) shouldBe None

      verify(dynamoClient, times(3)).getItem(
        BaseClient.DefaultCredentialTableName,
        Map("name" -> new AttributeValue("password"), "version" -> new AttributeValue(version.toString)).asJava
      )
    }
  }

  "SimpleCredStashClientTest and KMS" should {

    val aesKey = "aeskey".getBytes("UTF-8").padTo(32, 0.toByte)
    val hmacKey = "hmackey".getBytes("UTF-8").padTo(32, 0.toByte)
    val material = CredStashMaterial("password", "1", "pyLUqpGXvzVR", "af64dd6bd606ea78304dcd47541add1a5aec4aeb7a7359d411c7fb76076359dc", "NotRealKeyffffff")
    val queryResult = new QueryResult {
      setItems(List(material.asJavaMap).asJavaCollection)
    }
    val decryptResult = new DecryptResult() {
      this.setPlaintext(ByteBuffer.wrap(aesKey ++ hmacKey))
    }

    "decrypt a returned cred" in {

      when(dynamoClient.query(mockitoAny())).thenReturn(queryResult)
      when(kmsClient.decrypt(mockitoAny())).thenReturn(decryptResult)

      newClient.get("password") shouldBe Some("password1")
      verify(kmsClient, times(1)).decrypt(new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(Base64.decode(material.key))))
    }

    "decrypt a versioned cred" in {

      val material2 = CredStashMaterial("password", "2", "pyLUqpGXvzVS", "8286ef9d751be0d5a9b8b0a354bece7c2ab2ea274de01afab058d519ecabb2e2", "NotRealKeyffffff")

      when(dynamoClient.getItem(mockitoAny(), mockitoAny())).thenAnswer(new Answer[GetItemResult]() {
        override def answer(invocation: InvocationOnMock) = {
          val map = invocation.getArgumentAt(1, classOf[java.util.Map[String, AttributeValue]])
          map.get("version").getS match {
            case "1" => new GetItemResult().withItem(material.asJavaMap)
            case "2" => new GetItemResult().withItem(material2.asJavaMap)
          }
        }
      })

      when(kmsClient.decrypt(mockitoAny())).thenReturn(decryptResult)

      newClient.get("password", version = "2") shouldBe Some("password2")
      newClient.get("password", version = "1") shouldBe Some("password1")

      verify(kmsClient, times(2)).decrypt(new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(Base64.decode(material.key))))
    }

    "decrypt a cred with a context" in {

      when(dynamoClient.query(mockitoAny())).thenReturn(queryResult)
      when(kmsClient.decrypt(mockitoAny())).thenReturn(decryptResult)

      newClient.get("password", context = Map("env" -> "test")) shouldBe Some("password1")
      verify(kmsClient, times(1)).decrypt(
        new DecryptRequest()
          .withCiphertextBlob(ByteBuffer.wrap(Base64.decode(material.key)))
          .withEncryptionContext(Map("env" -> "test").asJava)
      )
    }

    "return None when KMS fails" in {

      when(dynamoClient.query(mockitoAny())).thenReturn(queryResult)
      when(kmsClient.decrypt(mockitoAny())).thenAnswer(new Answer[DecryptResult] {
        override def answer(invocation: InvocationOnMock) = throw new RuntimeException("oops")
      })

      newClient.get("password") shouldBe None
    }

    "return None when an HMAC doesn't match" in {

      val material3 = CredStashMaterial("password", "3", "pyLUqpGXvzVS", "invalidHmac", "NotRealKeyffffff")
      when(dynamoClient.getItem(mockitoAny(), mockitoAny())).thenReturn(new GetItemResult().withItem(material3.asJavaMap))
      when(kmsClient.decrypt(mockitoAny())).thenReturn(decryptResult)

      newClient.get("password", version = "3") shouldBe None
    }

  }

}
