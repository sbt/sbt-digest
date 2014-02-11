package com.typesafe.sbt

import sbt._
import sbt.Keys._
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web.SbtWebPlugin.WebKeys.stages
import org.apache.ivy.util.ChecksumHelper

object SbtDigest extends Plugin {

  object DigestKeys {
    val algorithms = SettingKey[Seq[String]]("digest-algorithms", "Types of checksum files to generate.")
    val addChecksums = TaskKey[Pipeline.Stage]("digest-add-checksums", "Add checksum files to asset pipeline.")
  }

  import DigestKeys._

  lazy val digestSettings: Seq[Setting[_]] = Seq(
    algorithms := Seq("md5"),
    includeFilter in addChecksums := AllPassFilter,
    excludeFilter in addChecksums := HiddenFileFilter,
    addChecksums <<= addChecksumFiles,
    stages <+= addChecksums
  )

  def addChecksumFiles: Def.Initialize[Task[Pipeline.Stage]] = Def.task { mappings =>
    val targetDir = target.value / "public-checksums"
    val include = (includeFilter in addChecksums).value
    val exclude = (excludeFilter in addChecksums).value
    val checksumMappings = for {
      (file, path) <- mappings if include.accept(file) && !exclude.accept(file)
      algorithm <- algorithms.value
    } yield {
      val checksum = ChecksumHelper.computeAsString(file, algorithm)
      val checksumPath = path + "." + algorithm
      val checksumFile = targetDir / checksumPath
      IO.write(checksumFile, checksum)
      (checksumFile, checksumPath)
    }
    mappings ++ checksumMappings
  }
}
