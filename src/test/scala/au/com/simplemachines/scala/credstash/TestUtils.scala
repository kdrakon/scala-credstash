package au.com.simplemachines.scala.credstash

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._

object TestUtils {

  implicit class CredStashMaterialUtils(material: CredStashMaterial) {
    def asJavaMap = {
      Map(
        "name" -> new AttributeValue(material.name),
        "version" -> new AttributeValue(material.version),
        "contents" -> new AttributeValue(material.contents),
        "hmac" -> new AttributeValue(material.hmac),
        "key" -> new AttributeValue(material.key)
      ).asJava
    }
  }

}
