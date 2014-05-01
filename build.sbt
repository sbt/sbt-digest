sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-digest"

version := "1.0.0-SNAPSHOT"

resolvers += Classpaths.sbtPluginSnapshots

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.0.0-SNAPSHOT")

// FIXME: Working around https://github.com/sbt/sbt/issues/1156#issuecomment-39317363
isSnapshot := true

publishMavenStyle := false

publishTo := {
  if (isSnapshot.value) Some(Classpaths.sbtPluginSnapshots)
  else Some(Classpaths.sbtPluginReleases)
}

scriptedSettings

scriptedLaunchOpts += ("-Dproject.version=" + version.value)
