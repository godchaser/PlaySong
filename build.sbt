name := "playsong"

version := "1.0"

lazy val `playsong` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( javaJdbc , javaEbean , cache , javaWs ,
  "org.hsqldb" % "hsqldb" % "2.3.2",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.apache.poi" % "poi" % "3.10-FINAL",
  "org.apache.poi" % "poi-ooxml" % "3.10-FINAL",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "fr.opensagres.xdocreport" % "org.apache.poi.xwpf.converter.pdf" % "1.0.5",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "bootstrap-select" % "1.6.3",
  "org.webjars" % "bootstrap" % "3.3.4",
  "org.webjars.bower" % "bootstrap-multiselect" % "0.9.12",
  "org.webjars.bower" % "datatables-responsive" % "1.0.6",
  "org.webjars.bower" % "datatables" % "1.10.7",
  "org.webjars.bower" % "typeahead.js" % "0.10.5",
  "org.webjars.bower" % "startbootstrap-sb-admin-2" % "1.0.0"
  )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
