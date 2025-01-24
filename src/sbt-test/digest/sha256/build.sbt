val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(digest)

// also create sha256 files

DigestKeys.algorithms += "sha256"

// for checking that the produced pipeline mappings are correct

val expected = Set(
  "css", "css/a.css", "css/a.css.md5", "css/a.css.sha256", "css/46a3aa6d97cccb6b28233d8e55ce4350-a.css", "css/7d2cfc655a208a03f1fdc5c8afdfa9721a65fb743ca37f565b6af09de3d356b5-a.css",
  "js", "js/a.js", "js/a.js.md5", "js/a.js.sha256", "js/2d4ecd06cd4648614f3f4f1bfc262512-a.js", "js/1ac0e4aa4363591314c2d539063cb5d8ac123088451795b1340ad7e4ac1d8204-a.js"
)

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
