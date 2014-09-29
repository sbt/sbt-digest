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
    digest := digestStage.value
  )

  def digestStage: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>
      val targetDir = webTarget.value / digest.key.label
      val include   = (includeFilter in digest).value
      val exclude   = (excludeFilter in digest).value
      val enabled   = algorithms.value
      DigestStage.run(mappings, enabled, include, exclude, targetDir)
  }

  object DigestStage {

    val DigestAlgorithms = Seq("md5", "sha1")

    sealed trait DigestMapping {
      def originalPath: String
      def mapping: PathMapping
    }

    case class OriginalMapping(originalPath: String, mapping: PathMapping) extends DigestMapping

    case class ChecksumMapping(originalPath: String, algorithm: String, checksum: String, mapping: PathMapping) extends DigestMapping

    case class VersionedMapping(originalPath: String, algorithm: String, checksum: String, mapping: PathMapping) extends DigestMapping

    sealed trait DigestOutput {
      def mappings: Seq[PathMapping]
    }

    case class Digested(original: Option[OriginalMapping], checksums: Seq[ChecksumMapping], versioned: Seq[VersionedMapping]) extends DigestOutput {
      def mappings = (original.toSeq ++ checksums ++ versioned).map(_.mapping)
    }

    case class Undigested(mapping: PathMapping) extends DigestOutput {
      def mappings = Seq(mapping)
    }

    /**
     * Add digest mappings for assets. Take into account existing checksums and versioned files.
     */
    def run(mappings: Seq[PathMapping], algorithms: Seq[String], include: FileFilter, exclude: FileFilter, targetDir: File): Seq[PathMapping] = {
      process(mappings, algorithms, include, exclude, targetDir) flatMap (_.mappings)
    }

    /**
     * Group together digest-related mappings and create any missing checksums or versioned mappings for each path.
     */
    def process(mappings: Seq[PathMapping], algorithms: Seq[String], include: FileFilter, exclude: FileFilter, targetDir: File): Seq[DigestOutput] = {
      val (ignored, filtered) = mappings partition { case (file, path) => file.isDirectory }
      val undigested  = ignored map Undigested
      val categorised = categorise(filtered, DigestAlgorithms)
      val grouped     = (categorised groupBy (_.originalPath)).toSeq
      val digested = grouped map {
        case (path, digestMappings) =>
          val original          = (digestMappings collect { case o: OriginalMapping => o }).headOption
          val originalFile      = original map { _.mapping._1 } filter { file => include.accept(file) && !exclude.accept(file) }
          val existingChecksums = digestMappings collect { case c: ChecksumMapping => c }
          val missingChecksums  = algorithms diff (existingChecksums map (_.algorithm))
          val newChecksums      = missingChecksums flatMap { algorithm => originalFile.map(file => generateChecksum(file, path, algorithm, targetDir)) }
          val checksums         = existingChecksums ++ newChecksums
          val existingVersioned = digestMappings collect { case v: VersionedMapping => v }
          val missingVersioned  = algorithms diff (existingVersioned map (_.algorithm))
          val newVersioned      = missingVersioned flatMap { algorithm => originalFile.map(file => generateVersioned(file, path, algorithm)) }
          val versioned         = existingVersioned ++ newVersioned
          Digested(original, checksums, versioned)
      }
      undigested ++ digested
    }

    /**
     * Categorise each mapping as original, checksum, or versioned.
     * Extract the original path, checksum, and algorithm.
     */
    def categorise(mappings: Seq[PathMapping], algorithms: Seq[String]): Seq[DigestMapping] = {
      mappings map { mapping =>
        checksumMapping(mapping, algorithms) orElse
        versionedMapping(mapping, algorithms) getOrElse
        originalMapping(mapping)
      }
    }

    /**
     * Create a digest mapping for an original file.
     */
    def originalMapping(mapping: PathMapping): OriginalMapping = {
      OriginalMapping(mapping._2, mapping)
    }

    /**
     * Check whether a mapping is for a checksum file.
     */
    def checksumMapping(mapping: PathMapping, algorithms: Seq[String]): Option[ChecksumMapping] = {
      val (file, path) = mapping
      val (base, name, ext) = splitPath(path)
      algorithms find ext.equals map { algorithm =>
        val original = buildPath(base, name, "")
        val checksum = IO.read(file)
        ChecksumMapping(original, algorithm, checksum, mapping)
      }
    }

    /**
     * Create a checksum file and mapping.
     */
    def generateChecksum(file: File, path: String, algorithm: String, targetDir: File): ChecksumMapping = {
      val checksum     = computeChecksum(file, algorithm)
      val checksumPath = path + "." + algorithm
      val checksumFile = targetDir / checksumPath
      IO.write(checksumFile, checksum)
      ChecksumMapping(path, algorithm, checksum, checksumFile -> checksumPath)
    }

    /**
     * Compute a checksum for a file. Supported algorithms are "md5" and "sha1".
     */
    def computeChecksum(file: File, algorithm: String): String = {
      ChecksumHelper.computeAsString(file, algorithm)
    }

    /**
     * Check whether a mapping is for a versioned file.
     */
    def versionedMapping(mapping: PathMapping, algorithms: Seq[String]): Option[VersionedMapping] = {
      val (file, path) = mapping
      val checksums = algorithms map { a => (a, computeChecksum(file, a)) }
      checksums find { case (_, checksum) => isVersioned(path, checksum) } map {
        case (algorithm, checksum) =>
          val original = unversioned(path, checksum)
          VersionedMapping(original, algorithm, checksum, mapping)
      }
    }

    /**
     * Create a versioned mapping with the checksum in the name.
     */
    def generateVersioned(file: File, path: String, algorithm: String): VersionedMapping = {
      // use original file with new versioned path, rather than copying
      val checksum = computeChecksum(file, algorithm)
      VersionedMapping(path, algorithm, checksum, file -> versioned(path, checksum))
    }

    /**
     * Check whether a path is versioned with the checksum.
     */
    def isVersioned(path: String, checksum: String): Boolean = {
      val (base, name, ext) = splitPath(path)
      name.startsWith(versionPrefix(checksum))
    }

    /**
     * Remove the checksum from a versioned path to get the original path.
     */
    def unversioned(path: String, checksum: String): String = {
      val (base, name, ext) = splitPath(path)
      buildPath(base, name.stripPrefix(versionPrefix(checksum)), ext)
    }

    /**
     * Add a checksum to the path name to create a versioned path.
     */
    def versioned(path: String, checksum: String): String = {
      val (base, name, ext) = splitPath(path)
      buildPath(base, versionPrefix(checksum) + name, ext)
    }

    /**
     * Create the checksum prefix.
     */
    def versionPrefix(checksum: String): String = checksum + "-"

    /**
     * Get the base path, name, and extension for a path.
     */
    def splitPath(path: String): (String, String, String) = {
      val pathFile    = file(path)
      val parent      = pathFile.getParent
      val base        = if (parent eq null) "" else parent
      val (name, ext) = pathFile.baseAndExt
      (base, name, ext)
    }

    /**
     * Create a path from base, name, and extension parts.
     */
    def buildPath(base: String, name: String, ext: String): String = {
      val suffix = if (ext.isEmpty) "" else ("." + ext)
      (file(base) / (name + suffix)).getPath
    }
  }

}
