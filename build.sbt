ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / organization := "jp.ed.nnn"
ThisBuild / scalaVersion := "3.3.3"

val osName = settingKey[String]("osName")

osName := (System.getProperty("os.name") match {
  case name if name.startsWith("Linux")   => "linux"
  case name if name.startsWith("Mac")     => "mac"
  case name if name.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
})

libraryDependencies ++= Seq(
  "org.openjfx" % "javafx-base"     % "21" classifier osName.value,
  "org.openjfx" % "javafx-controls" % "21" classifier osName.value,
  "org.openjfx" % "javafx-fxml"     % "21" classifier osName.value,
  "org.openjfx" % "javafx-graphics" % "21" classifier osName.value,
  "org.openjfx" % "javafx-web"      % "21" classifier osName.value,
  "org.openjfx" % "javafx-media"    % "21" classifier osName.value
)
