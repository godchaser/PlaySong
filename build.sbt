name := "playsong"

version := "1.0"

lazy val `playsong` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)

routesGenerator := InjectedRoutesGenerator

lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)

libraryDependencies ++= Seq( javaJdbc , cache , javaWs , filters, evolutions,
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "6.13.5",
  "org.seleniumhq.selenium" % "selenium-java" % "2.48.2",
  "org.hsqldb" % "hsqldb" % "2.3.3",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "org.apache.poi" % "poi" % "3.13",
  "org.apache.poi" % "poi-ooxml" % "3.13",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "fr.opensagres.xdocreport" % "org.apache.poi.xwpf.converter.pdf" % "1.0.5",
  "com.itextpdf" % "itextpdf" % "5.5.8",
  "org.eclipse.persistence" % "eclipselink" % "2.6.2",
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

TwirlKeys.templateImports ++= Seq("models.helpers._")
