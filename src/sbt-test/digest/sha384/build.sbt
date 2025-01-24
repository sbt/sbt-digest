val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(digest)

// also create sha256 files

DigestKeys.algorithms += "sha384"

// for checking that the produced pipeline mappings are correct

val expected = Set(
  "css", "css/a.css", "css/a.css.md5", "css/a.css.sha384", "css/46a3aa6d97cccb6b28233d8e55ce4350-a.css", "css/efa4b005ec16da2e6124ab6fb1c7a11329f7eda45d3d9972b27995b94a09c048c54a408308115d7118d21ee18878e4e9-a.css",
  "js", "js/a.js", "js/a.js.md5", "js/a.js.sha384", "js/2d4ecd06cd4648614f3f4f1bfc262512-a.js", "js/dd3b40496deaa6e6ca76b6a6bb145f966839ed08d812cfbc297c312f31c819c96afa750b34a2bfad337a0622172307e4-a.js"
)

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
