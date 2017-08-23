lazy val `sbt-digest` = project in file(".")

description := "sbt-web plugin for adding checksum files for web assets. Checksums are useful for asset fingerprinting and etag values"

addSbtJsEngine("1.2.2")
