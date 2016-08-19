package au.com.simplemachines.scala.credstash.reader

trait CredValueReader[K] {
  def read(value: String): K
}

object Readers {
  implicit def asString = CredValueStringReader
  implicit def asInt = CredValueNumericReaders.CredValueIntReader
  implicit def asLong = CredValueNumericReaders.CredValueLongReader
}

object CredValueStringReader extends CredValueReader[String] {
  override def read(value: String) = value
}

object CredValueNumericReaders {

  object CredValueIntReader extends CredValueReader[Int] {
    override def read(value: String) = value.toInt
  }

  object CredValueLongReader extends CredValueReader[Long] {
    override def read(value: String) = value.toLong
  }
}
