package au.com.simplemachines.scala.credstash

trait CredValueReader[K] {
  def read(value: String): K
}

object CredValueStringReader extends CredValueReader[String]
{
  override def read(value: String): String = value
}
