# scala-credstash

[![Build Status](https://travis-ci.org/kdrakon/scala-credstash.svg?branch=master)](https://travis-ci.org/kdrakon/scala-credstash)
[![](https://jitpack.io/v/kdrakon/scala-credstash.svg)](https://jitpack.io/#kdrakon/scala-credstash)

A lightweight and read-only implementation of the [credstash](https://github.com/fugue/credstash) client. Now you can read your KMS encrypted data directly from DynamoDB into your Scala code.

### Dependencies
- As credstash utilises 128 bit AES encryption, your Java runtime needs the [JCE](https://en.wikipedia.org/wiki/Java_Cryptography_Extension) _Unlimited Strength Jurisdiction Policy Files_ or not be limited by it.
- The necessary configuration needed for credstash (AWS credentials, KMS, a DynamoDB, etc.). See their dependencies listing and setup guide [here](https://github.com/fugue/credstash#dependencies).

### SimpleCredStashClient
Create a `SimpleCredStashClient` using AWS' KMS and DynamoDB clients:

```scala
  val creds = new DefaultAWSCredentialsProviderChain()
  
  val kmsClient: AWSKMSClient = new AWSKMSClient(creds) { self =>
    setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
  }

  val dynamoClient: AmazonDynamoDBClient = new AmazonDynamoDBClient(creds) { self =>
    setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
  }

  val credstash = SimpleCredStashClient(kmsClient, dynamoClient)
```
You can then read a String value *or* other type using a custom `CredStashValueReader`:
```scala
val password = credstash.get("password")

import io.policarp.scala.credstash.reader.Readers._
val timeout = credstash.as[Int]("timeout")
```
