lazy val root = (project in file(".")).enablePlugins(SbtWeb)

// also run the digest stage twice

pipelineStages := Seq(digest, digest)

// only add new checksums and versioned files for .js

includeFilter in digest := "*.js"

// for checking that the produced pipeline mappings are correct

val expected = Set(
  "css",
  "css/a.css",
  "css/b.css", "css/b.css.md5",
  "css/c.css", "css/c.css.md5", "css/f66bcfb539dfd672baf47402fbd2804c-c.css",
  "css/d.css", "css/d.css.sha1", "css/73c9d28a8d2970eaa61b0fc3f91a2cfc3816747d-d.css",
  "css/e.css.md5", "css/5a5bb2504a8bb8b222766f27b279542e-e.css",
  "css/ab2b049fa2c74cfae35b7b629259a382-f.css",
  "js",
  "js/a.js", "js/a.js.md5", "js/800f8a8242c581c3075343bcc1995e16-a.js",
  "js/b.js", "js/b.js.md5", "js/8f4a26afc0d7bf5fbd0e05e96a380c0e-b.js",
  "js/c.js", "js/c.js.md5", "js/10a5b9df19bf35865246507d1542c743-c.js",
  "js/d.js", "js/d.js.md5", "js/0346e2e7d668f046cb2e8a831915dfc3-d.js",
  "js/d.js", "js/d.js.sha1", "js/8a064dc23798d5e5771b55df3585c6cfdb698efc-d.js",
  "js/e.js.md5", "js/02c45f94eb87f048b5df3909895f78a2-e.js",
  "js/623cc43ffbcf19c2d473ac0c0c4aeb1a-f.js"
)

val checkMappings = taskKey[Unit]("check the pipeline mappings")

checkMappings := {
  val mappings = WebKeys.pipeline.value
  val paths = (mappings map (_._2)).toSet
  if (paths != expected) sys.error(s"Expected $expected but pipeline paths are $paths")
}
