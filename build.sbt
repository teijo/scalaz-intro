scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7.2a"
)

mainClass in Compile := Some("ScalazIntro")
