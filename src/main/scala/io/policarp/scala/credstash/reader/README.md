##CredValueReader

A reader simply defines how a String value decrypted from credstash should be interpreted. `CredValueStringReader` simply returns you the String value, but you can provide your own reader's to transform data to other types (e.g. JSON, HOCON-compatible types).