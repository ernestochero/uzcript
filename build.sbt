lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "io.nem" % "symbol-sdk-vertx-client" % "0.17.1",
      "com.vmunier" %% "scalajs-scripts" % "1.1.4",
      "dev.zio" %% "zio" % "1.0.0-RC18-2",
      "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC12",
      "com.github.pureconfig" %% "pureconfig" % "0.12.1",
      guice,
      specs2 % Test
    )
  )
  .enablePlugins(PlayScala)
  .dependsOn(sharedJvm)

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq("org.scala-js" %%% "scalajs-dom" % "1.0.0")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings =
  Seq(scalaVersion := "2.13.1", organization := "com.ernestochero")