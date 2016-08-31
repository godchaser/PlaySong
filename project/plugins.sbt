logLevel := Level.Warn

// old repo
//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
// The Typesafe repository
resolvers += "Typesafe repository" at "https://dl.bintray.com/typesafe/maven-releases/"

resolvers += "Madoushi sbt-plugins" at "https://dl.bintray.com/madoushi/sbt-plugins/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.6")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "3.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")
addSbtPlugin("org.madoushi.sbt" % "sbt-sass" % "0.9.3")