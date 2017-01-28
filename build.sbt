name := "translate"

scalaVersion := "2.11.8"

// Ensure compilation with java 8
javacOptions ++= Seq("-source", "1.8")
javacOptions in (Compile, Keys.compile) ++= Seq("-g", "-target", "1.8", "-Xlint")

resolvers ++= Seq(
  "Twitter Maven" at "https://maven.twttr.com",
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % "2.2.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
    "net.ruippeixotog" %% "scala-scraper" % "1.2.0"
)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerExposedPorts := Seq(
  8888, // HTTP
  9990  // Admin
)
