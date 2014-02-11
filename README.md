sbt-digest
==========

[sbt-web] plugin for adding checksum files for web assets.


Add plugin
----------

Add the plugin to `project/plugins.sbt`. For example:

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0-SNAPSHOT")
```


Add settings
------------

In a `build.sbt` file, add the `digestSettings` after `webSettings`. For example:

```scala
webSettings

digestSettings
```

To create checksums for assets produced or modified by other sbt-web plugins,
place the `digestSettings` after the settings for these other plugins.


Configuration
-------------

### Algorithms

Supported hash algorithms are `md5` and `sha1`. The default is to only create
`md5` checksum files. To configure this, modify the `algorithms`
setting. For example, to also generate`sha1` checksum files:

```scala
DigestKeys.algorithms += "sha1"
```

### Filters

Include and exclude filters can be provided. For example, to only create
checksum files for `.js` files:

```scala
includeFilter in DigestKeys.addChecksums := "*.js"
```

Or to exclude all `.js` files but include any other files:

```scala
excludeFilter in DigestKeys.addChecksums := "*.js"
```


License
-------

This code is licensed under the [Apache 2.0 License][apache].


[sbt-web]: https://github.com/sbt/sbt-web
[apache]: http://www.apache.org/licenses/LICENSE-2.0.html
