name := "playsong"

version := "1.0"

lazy val `playsong` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( javaJdbc , javaEbean , cache , javaWs , filters,
  "org.hsqldb" % "hsqldb" % "2.3.3",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "org.apache.poi" % "poi" % "3.13",
  "org.apache.poi" % "poi-ooxml" % "3.13",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "fr.opensagres.xdocreport" % "org.apache.poi.xwpf.converter.pdf" % "1.0.5",
  "com.itextpdf" % "itextpdf" % "5.5.7",
  "org.eclipse.persistence" % "eclipselink" % "2.6.1",
  "org.webjars.bower" % "jquery" % "2.1.4",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars.bower" % "bootstrap-select" % "1.7.5",
  "org.webjars.bower" % "bootstrap-multiselect" % "0.9.13",
  "org.webjars.bower" % "datatables-responsive" % "1.0.6",
  "org.webjars.bower" % "datatables.net-bs" % "1.10.10",
  "org.webjars.bower" % "typeahead.js" % "0.11.1",
  "org.webjars" % "startbootstrap-sb-admin-2" % "1.0.7",
  "org.webjars.bower" % "font-awesome" % "4.5.0",
  "org.webjars" % "handsontable" % "0.15.0-beta2",
  "org.yaml" % "snakeyaml" % "1.15"
  )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
