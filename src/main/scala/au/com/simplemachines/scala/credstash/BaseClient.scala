package au.com.simplemachines.scala.credstash

import au.com.simplemachines.scala.credstash.reader.CredValueReader

object BaseClient {

  type EncryptionContext = java.util.Map[String, String]
  val EmptyEncryptionContext: EncryptionContext = new java.util.HashMap[String, String]()

  val DefaultCredentialTableName = "credential-store"
  val DefaultCharacterEncoding = "UTF-8"
}

trait BaseClient {

  def get[K](name: String, table: String = BaseClient.DefaultCredentialTableName, version: String = "-1")(implicit reader: CredValueReader[K]): Option[K]

}

trait AmazonClients {
  type KmsClient
  type DynamoClient

  val kmsClient: KmsClient
  val dynamoClient: DynamoClient
}

trait EncryptionClients {
  val aesEncryption: AESEncryption
}
