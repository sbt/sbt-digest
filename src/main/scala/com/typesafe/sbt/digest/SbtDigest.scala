package com.typesafe.sbt.digest

import sbt._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys._
import org.apache.ivy.util.ChecksumHelper
import sbt.Task

object Import {

  object DigestKeys {
    val algorithms = SettingKey[Seq[String]]("digest-algorithms", "Types of checksum files to generate.")
    val addChecksums = TaskKey[Pipeline.Stage]("digest-add-checksums", "Add checksum files to asset pipeline.")
  }

}

object SbtDigest extends AutoPlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport.DigestKeys._

  override def projectSettings: Seq[Setting[_]] = Seq(
    algorithms := Seq("md5"),
    includeFilter in addChecksums := AllPassFilter,
    excludeFilter in addChecksums := HiddenFileFilter,
    addChecksums := checksumFiles.value,
    pipelineStages <+= addChecksums
  )

  def checksumFiles: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>
      val targetDir = webTarget.value / addChecksums.key.label
      val include = (includeFilter in addChecksums).value
      val exclude = (excludeFilter in addChecksums).value
      val checksumMappings = for {
        (file, path) <- mappings if !file.isDirectory && include.accept(file) && !exclude.accept(file)
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
