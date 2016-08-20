package au.com.simplemachines.scala.credstash

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.kms.AWSKMSClient
import org.mockito.Mockito
import org.scalatest.{ mock => _, _ }
import org.scalatest.mockito.MockitoSugar._

trait SimpleCredStashClientTestHarness extends WordSpec with BeforeAndAfterEach {

  val kmsClient = mock[AWSKMSClient]
  val dynamoClient = mock[AmazonDynamoDBClient]

  def newClient = SimpleCredStashClient(kmsClient, dynamoClient, DefaultAESEncryption)

  override def beforeEach() = {
    Mockito.reset(kmsClient, dynamoClient)
  }

}
