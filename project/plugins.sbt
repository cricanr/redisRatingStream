logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.16")
addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.34")
