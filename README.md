# scala-credstash

A Scala implementation of the [credstash](https://github.com/fugue/credstash) client. Now you can read your KMS encrypted data directly from DynamoDB into your Scala code.

### Dependencies
- As credstash utilises 128 bit AES encryption, your Java runtime needs the [JCE](https://en.wikipedia.org/wiki/Java_Cryptography_Extension).
- The necessary configuration needed for credstash (AWS credentials, KMS, a DynamoDB, etc.). See their dependencies listing and setup guide [here](https://github.com/fugue/credstash#dependencies).

### State
Still a work in progress, but you can play around with the `SimpleCredStashClient`, which provides read capabilities of your stashed data.