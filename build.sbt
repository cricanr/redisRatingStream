name := "redisRatingStream"
 
version := "1.0" 
      
lazy val `redisratingstream` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"


//START
enablePlugins(JavaAppPackaging, DockerComposePlugin)
dockerImageCreationTask := (publishLocal in Docker).value

//END
val circeVersion = "0.9.3"
libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.13" % Test,
  "net.debasishg" %% "redisclient" % "3.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )