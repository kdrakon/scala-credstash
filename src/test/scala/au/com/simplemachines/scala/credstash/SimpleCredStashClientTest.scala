package au.com.simplemachines.scala.credstash

import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, GetItemResult, QueryResult }
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

      verify(dynamoClient, times(2)).getItem(
        BaseClient.DefaultCredentialTableName,
        Map("name" -> new AttributeValue("password"), "version" -> new AttributeValue(version.toString)).asJava
      )
    }
  }

  "SimpleCredStashClientTest and KMS" should {

    "decrypt a returned cred" in {

    }

    "decrypt a versioned cred" in {

    }

    "decrypt a cred with a context" in {

    }

  }

}
