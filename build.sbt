name := "playsong"

version := "1.0"

lazy val `playsong` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( javaJdbc , javaEbean , cache , javaWs , "org.hsqldb" % "hsqldb" % "2.3.2", "postgresql" % "postgresql" % "9.1-901-1.jdbc4")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
