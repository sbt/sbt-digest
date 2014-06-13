lazy val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(digest)

// for checking that the produced pipeline mappings are correct

val expected = Set(
  "css", "css/a.css", "css/a.css.md5", "css/2f12635c315dfd8c7d3a68c857736b0e-a.css",
  "js", "js/a.js", "js/a.js.md5", "js/d41d8cd98f00b204e9800998ecf8427e-a.js",
  "img", "img/remove.png", "img/remove.png.md5", "img/08e31f7ccd6c10d6133027cd5173b0ac-remove.png"
)

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
