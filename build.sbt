name := "playsong"

version := "1.0"

lazy val `playsong` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( javaJdbc , javaEbean , cache , javaWs , filters,
  "org.hsqldb" % "hsqldb" % "2.3.2",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.apache.poi" % "poi" % "3.10-FINAL",
  "org.apache.poi" % "poi-ooxml" % "3.10-FINAL",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "fr.opensagres.xdocreport" % "org.apache.poi.xwpf.converter.pdf" % "1.0.5",
  "com.itextpdf" % "itextpdf" % "5.5.6",
  "org.eclipse.persistence" % "eclipselink" % "2.6.1-RC1",
  "org.webjars.bower" % "jquery" % "2.1.3",
  "org.webjars.bower" % "bootstrap-select" % "1.6.4",
  "org.webjars.bower" % "bootstrap" % "3.3.4",
  "org.webjars.bower" % "bootstrap-multiselect" % "0.9.12",
  "org.webjars.bower" % "datatables-responsive" % "1.0.6",
  "org.webjars.bower" % "datatables" % "1.10.7",
  "org.webjars.bower" % "typeahead.js" % "0.10.5",
  "org.webjars.bower" % "startbootstrap-sb-admin-2" % "1.0.0",
  "org.webjars" % "metisMenu" % "1.1.3",
  "org.webjars" % "handsontable" % "0.15.0-beta2",
  "org.yaml" % "snakeyaml" % "1.15"
  )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
