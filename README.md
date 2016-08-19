# scala-credstash

[![Build Status](https://travis-ci.org/simple-machines/scala-credstash.svg?branch=master)](https://travis-ci.org/simple-machines/scala-credstash)

A Scala implementation of the [credstash](https://github.com/fugue/credstash) client. Now you can read your KMS encrypted data directly from DynamoDB into your Scala code. This client is meant to be lightweight and not a replacement to the Python client.

### Dependencies
- As credstash utilises 128 bit AES encryption, your Java runtime needs the [JCE](https://en.wikipedia.org/wiki/Java_Cryptography_Extension) _Unlimited Strength Jurisdiction Policy Files_ or not be limited by it.
- The necessary configuration needed for credstash (AWS credentials, KMS, a DynamoDB, etc.). See their dependencies listing and setup guide [here](https://github.com/fugue/credstash#dependencies).

### State
Still a work in progress, but you can play around with the `SimpleCredStashClient`, which provides read capabilities of your stashed data.

```
  val creds = new DefaultAWSCredentialsProviderChain()
  
  val kmsClient: AWSKMSClient = new AWSKMSClient(creds) { self =>
    setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
  }

  val dynamoClient: AmazonDynamoDBClient = new AmazonDynamoDBClient(creds) { self =>
    setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
  }

  val client = SimpleCredStashClient(kmsClient, dynamoClient)
```
You can then read a value like this using a `CredStashValueReader`:
```
import au.com.simplemachines.scala.credstash.reader.Readers._
val value = client.get[String]("seantest")
```