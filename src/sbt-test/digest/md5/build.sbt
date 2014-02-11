webSettings

digestSettings

// only create md5 files

DigestKeys.algorithms := Seq("md5")

// for checking that the produced pipeline mappings are correct

val expected = Set("css/a.css", "css/a.css.md5", "js/a.js", "js/a.js.md5")

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
