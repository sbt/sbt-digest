import DigestKeys.addChecksums

webSettings

digestSettings

// set an exclude filter for css files

excludeFilter in addChecksums := "*.css"

// for checking that the produced pipeline mappings are correct

val expected = Set("css/a.css", "js/a.js", "js/a.js.md5", "js/a.js.sha1")

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
