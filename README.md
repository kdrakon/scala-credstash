# scala-credstash

A Scala implementation of the [credstash](https://github.com/fugue/credstash) client. Now you can read your KMS encrypted data directly from DynamoDB into your Scala code. This client is meant to be lightweight and not a replacement to the Python client.

### Dependencies
- As credstash utilises 128 bit AES encryption, your Java runtime needs the [JCE](https://en.wikipedia.org/wiki/Java_Cryptography_Extension) _Unlimited Strength Jurisdiction Policy Files_ or not be limited by it.
- The necessary configuration needed for credstash (AWS credentials, KMS, a DynamoDB, etc.). See their dependencies listing and setup guide [here](https://github.com/fugue/credstash#dependencies).

### State
Still a work in progress, but you can play around with the `SimpleCredStashClient`, which provides read capabilities of your stashed data.

```
  val client = new SimpleCredStashClient {
    
    val creds = new DefaultAWSCredentialsProviderChain()
    
    override val kmsClient: AWSKMSClient = new AWSKMSClient(creds) { self =>
      setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
    }

    override val dynamoClient: AmazonDynamoDBClient = new AmazonDynamoDBClient(creds) { self =>
      setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
    }

    override val aesEncryption = DefaultAESEncryption
  }
```
You can then read a value like this using a `CredStashValueReader`:
```
val value = client.get("seantest")(CredValueStringReader)
```