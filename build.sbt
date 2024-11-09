name := """tubelytics"""
organization := "TubeLytics"

version := "1.0-SNAPSHOT"

jacocoReportSettings := JacocoReportSettings()
  .withThresholds(
    JacocoThresholds(
      instruction = 10,
      method = 100,
      branch = 10,
      complexity = 10,
      line = 100,
      clazz = 100)
  )

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
javacOptions += "-Xlint:deprecation"

libraryDependencies += guice
libraryDependencies += "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"

libraryDependencies += caffeine
libraryDependencies += "org.mockito" % "mockito-core" % "3.6.0"
libraryDependencies += "org.junit.jupiter" % "junit-jupiter" % "5.7.0" % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test






