import com.typesafe.sbt.web.SbtWeb

val root = project.in(file(".")).addPlugins(SbtWeb)

// also create sha1 files

DigestKeys.algorithms += "sha1"

// for checking that the produced pipeline mappings are correct

val expected = Set("css", "css/a.css", "css/a.css.md5", "css/a.css.sha1", "js", "js/a.js", "js/a.js.md5", "js/a.js.sha1")

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
