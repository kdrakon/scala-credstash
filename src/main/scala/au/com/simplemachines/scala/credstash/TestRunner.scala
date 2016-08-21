package au.com.simplemachines.scala.credstash

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.kms.AWSKMSClient

object TestRunner extends App {

  val creds = new DefaultAWSCredentialsProviderChain()

  val kmsClient: AWSKMSClient = new AWSKMSClient(creds) { self =>
    setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
  }

  val dynamoClient: AmazonDynamoDBClient = new AmazonDynamoDBClient(creds) { self =>
    setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
  }

  val client = SimpleCredStashClient(kmsClient, dynamoClient)

  import au.com.simplemachines.scala.credstash.reader.Readers._
  val value = client.as[String]("seantest")
  assert(client.get("seantest") == client.as[String]("seantest"))
  assert(client.as[Int]("seantest2").contains(1337))
  println(value)
  println(client.as[Int]("seantest2"))
}