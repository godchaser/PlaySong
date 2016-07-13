logLevel := Level.Warn

// old repo
//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
// The Typesafe repository
resolvers += "Typesafe repository" at "https://dl.bintray.com/typesafe/maven-releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "3.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")