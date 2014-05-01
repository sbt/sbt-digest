val root = (project in file(".")).addPlugins(SbtWeb)

pipelineStages := Seq(digest)

// also create sha1 files

DigestKeys.algorithms += "sha1"

// for checking that the produced pipeline mappings are correct

val expected = Set(
  "css", "css/a.css", "css/a.css.md5", "css/a.css.sha1", "css/d41d8cd98f00b204e9800998ecf8427e-a.css", "css/da39a3ee5e6b4b0d3255bfef95601890afd80709-a.css",
  "js", "js/a.js", "js/a.js.md5", "js/a.js.sha1", "js/d41d8cd98f00b204e9800998ecf8427e-a.js", "js/da39a3ee5e6b4b0d3255bfef95601890afd80709-a.js"
)

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
