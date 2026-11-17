import com.github.sbt.digest.util.ChecksumHelper
import java.io.IOException

ThisBuild / scalaVersion := {
  if (sbtVersion.value.startsWith("1.")) "2.12.21" else "3.9.0"
}

commands += Command.command("checkChecksumFormats") { state =>
  IO.withTemporaryDirectory { directory =>
    val input = directory / "data.txt"
    val checksum = directory / "data.txt.md5"
    val expected = "5d41402abc4b2a76b9719d911017c592"
    IO.write(input, "hello")

    val formats = Seq(
      expected + "\n",
      expected.toUpperCase(java.util.Locale.US) + "\n",
      expected + "  data.txt\n",
      "MD5 (data.txt) = " + expected + "\n",
      "data.txt: " + expected.grouped(2).mkString(" ") + "\n"
    )
    formats.foreach { contents =>
      IO.write(checksum, contents)
      ChecksumHelper.check(input, checksum, "md5")
    }

    IO.write(checksum, "00000000000000000000000000000000")
    val rejected = try {
      ChecksumHelper.check(input, checksum, "md5")
      false
    } catch {
      case _: IOException => true
    }
    assert(rejected, "An incorrect checksum must be rejected")
  }
  state
}
