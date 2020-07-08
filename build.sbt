val playVersion = "2.8.1"
lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "io.nem" % "symbol-sdk-vertx-client" % "0.19.0",
      "com.vmunier" %% "scalajs-scripts" % "1.1.4",
      "dev.zio" %% "zio" % "1.0.0-RC21",
      "org.typelevel" %% "cats-effect" % "2.1.3",
      "dev.zio" %% "zio-interop-cats" % "2.1.3.0-RC16",
      "com.github.pureconfig" %% "pureconfig" % "0.12.1",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
      guice,
      specs2 % Test
    ),
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
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
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % playVersion
    )
  )
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings =
  Seq(scalaVersion := "2.13.1", organization := "com.ernestochero")
