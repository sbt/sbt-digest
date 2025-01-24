val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(digest)

// also create sha256 files

DigestKeys.algorithms += "sha512"

// for checking that the produced pipeline mappings are correct

val expected = Set(
  "css", "css/a.css", "css/a.css.md5", "css/a.css.sha512", "css/46a3aa6d97cccb6b28233d8e55ce4350-a.css", "css/58b6708177852235ee2a17000347395ea371b7456c907e4fc47d7b42694ae01dad0f946bf6def6fc0164e6aef68de50d0c64edc45b46805079c3ab98e837f33e-a.css",
  "js", "js/a.js", "js/a.js.md5", "js/a.js.sha512", "js/2d4ecd06cd4648614f3f4f1bfc262512-a.js", "js/346677e51f420a89577ec4f66a0eec2872a6fef3f0f54f45a61115fdc2f650e59c97916618ba79671f977cbd51f1557bc37338451d31cec2380d274480c9509f-a.js"
)

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
