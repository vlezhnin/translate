name := "translate"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Twitter Maven" at "https://maven.twttr.com",
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % "2.2.0"
)
