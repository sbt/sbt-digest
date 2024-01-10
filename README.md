sbt-digest
==========

[sbt-web] plugin for adding checksum files for web assets. Checksums are useful for asset fingerprinting and etag values.

The plugin works by prepending a digest to the asset name. The default digest algorithm used is MD5.
In addition an .md5 file (depending on the algorithm) is also generated containing the hash.
The hash can then be used to lookup the associated hashed file.

You may have an asset such as:

    ./target/web/public/test/public/images/example.png

sbt-digest will create a second copy of the file with the hash and a digest file:

    ./target/web/digest/images/23dcc403b263f262692ac58437104acf-example.png
    ./target/web/digest/images/example.png.md5

[![Build Status](https://github.com/sbt/sbt-digest/actions/workflows/build-test.yml/badge.svg)](https://github.com/sbt/sbt-digest/actions/workflows/build-test.yml)

Add plugin
----------

Add the plugin to `project/plugins.sbt`. For example:

```scala
addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.0.0")
```

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

    lazy val root = (project in file(".")).enablePlugins(SbtWeb)

As with all sbt-web asset pipeline plugins you must declare their order of execution e.g.:

```scala
pipelineStages := Seq(digest)
```

Configuration
-------------

### Algorithms

Supported hash algorithms are `md5` and `sha1`. The default is to only create `md5` checksum files. To configure this, modify the `algorithms` setting.
For example, to also generate`sha1` checksum files:

```scala
DigestKeys.algorithms += "sha1"
```

### Filters

Include and exclude filters can be provided. For example, to only create checksum files for `.js` files:

```scala
digest / includeFilter := "*.js"
```

Or to exclude all `.js` files but include any other files:

```scala
digest / excludeFilter := "*.js"
```


Contribution policy
-------------------

Contributions via GitHub pull requests are gladly accepted from their original author. 

License
-------

This code is licensed under the [Apache 2.0 License][apache].

[sbt-web]: https://github.com/sbt/sbt-web
[apache]: http://www.apache.org/licenses/LICENSE-2.0.html
