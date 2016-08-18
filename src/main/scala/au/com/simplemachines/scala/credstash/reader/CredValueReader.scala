package au.com.simplemachines.scala.credstash.reader

trait CredValueReader[K] {
  def read(value: String): K
}

object CredValueStringReader extends CredValueReader[String]
{
  override def read(value: String): String = value
}
