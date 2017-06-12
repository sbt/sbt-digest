lazy val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(digest)

DigestKeys.indexFile := Some((resourceManaged in Compile).value / "assets.index.json")

// write a sorted index file for testing
DigestKeys.indexWriter := { index =>
  (index.toSeq.sortBy(_._2).map {
    case (original, versioned) => s"'$original': '$versioned'"
  }).mkString("\n")
}

// for checking that the produced pipeline mappings are correct

val expected = Set(
  "css", "css/a.css", "css/a.css.md5", "css/d41d8cd98f00b204e9800998ecf8427e-a.css",
  "js", "js/a.js", "js/a.js.md5", "js/d41d8cd98f00b204e9800998ecf8427e-a.js"
)

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
