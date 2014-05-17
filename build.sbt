sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-digest"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

resolvers += Classpaths.sbtPluginSnapshots

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.0.0-SNAPSHOT")

publishMavenStyle := false

publishTo := {
  if (isSnapshot.value) Some(Classpaths.sbtPluginSnapshots)
  else Some(Classpaths.sbtPluginReleases)
}

scriptedSettings

scriptedLaunchOpts += ("-Dproject.version=" + version.value)
