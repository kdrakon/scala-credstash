package io.policarp.scala.credstash

import com.amazonaws.services.dynamodbv2.model.AttributeValue

case class CredStashMaterial(name: String, version: String, contents: String, hmac: String, key: String)

object CredStashMaterial {
  def apply(existing: java.util.Map[String, AttributeValue]): CredStashMaterial = {
    CredStashMaterial(
      Option(existing.get("name")).fold("")(_.getS),
      Option(existing.get("version")).fold("")(_.getS),
      Option(existing.get("contents")).fold("")(_.getS),
      Option(existing.get("hmac")).fold("")(_.getS),
      Option(existing.get("key")).fold("")(_.getS)
    )
  }
}