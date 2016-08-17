package au.com.simplemachines.scala.credstash

object BaseClient {

  type EncryptionContext = java.util.Map[String, String]
  val EmptyEncryptionContext: EncryptionContext = new java.util.HashMap[String, String]()

  val defaultCredentialTableName = "credential-store"
}

trait AmazonClients {
  type KmsClient
  type DynamoClient

  val kmsClient: KmsClient
  val dynamoClient: DynamoClient
}

trait BaseClient extends AmazonClients {

  def get[K](name: String, table: String = BaseClient.defaultCredentialTableName, version: String = "-1")(implicit reader: CredValueReader[K]): Option[K]

}
