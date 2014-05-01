package com.typesafe.sbt.digest

import sbt._
import com.typesafe.sbt.web.{PathMapping, SbtWeb}
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys._
import org.apache.ivy.util.ChecksumHelper
import sbt.Task

object Import {

  val digest = TaskKey[Pipeline.Stage]("digest", "Add checksum files to asset pipeline.")

  object DigestKeys {
    val algorithms = SettingKey[Seq[String]]("digest-algorithms", "Types of checksum files to generate.")
  }

}

object SbtDigest extends AutoPlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport._
  import DigestKeys._

  override def projectSettings: Seq[Setting[_]] = Seq(
    algorithms := Seq("md5"),
    includeFilter in digest := AllPassFilter,
    excludeFilter in digest := HiddenFileFilter,
    digest := checksumFiles.value
  )

  private def generateChecksumFiles(file: File, path: String, algorithm: String, targetDir: File): Seq[PathMapping] = {
    val checksum = ChecksumHelper.computeAsString(file, algorithm)
    val checksumPath = path + "." + algorithm
    val checksumFile = targetDir / checksumPath
    IO.write(checksumFile, checksum)
    val pathFile = sbt.file(path)
    val checksummedPath = (pathFile.getParentFile / (checksum + "-" + pathFile.getName)).getPath
    val checksummedFile = targetDir / checksummedPath
    IO.copyFile(file, checksummedFile)
    Seq((checksumFile, checksumPath), (checksummedFile, checksummedPath))
  }

  def checksumFiles: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>
      val targetDir = webTarget.value / digest.key.label
      val include = (includeFilter in digest).value
      val exclude = (excludeFilter in digest).value
      val checksumMappings: Seq[PathMapping] = for {
        (file, path) <- mappings if !file.isDirectory && include.accept(file) && !exclude.accept(file)
        algorithm <- algorithms.value
        mapping <- generateChecksumFiles(file, path, algorithm, targetDir)
      } yield mapping
      mappings ++ checksumMappings
  }
}
