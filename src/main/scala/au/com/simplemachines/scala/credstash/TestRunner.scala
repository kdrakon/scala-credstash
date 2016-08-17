package au.com.simplemachines.scala.credstash

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.kms.AWSKMSClient

object TestRunner extends App {

  val client = new SimpleCredStashClient {
    val creds = new DefaultAWSCredentialsProviderChain()
    override val kmsClient: AWSKMSClient = new AWSKMSClient(creds) { self =>
      setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
    }

    override val dynamoClient: AmazonDynamoDBClient = new AmazonDynamoDBClient(creds) { self =>
      setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
    }
  }

  val value = client.get("seantest")(CredValueStringReader)
  println(value)

}