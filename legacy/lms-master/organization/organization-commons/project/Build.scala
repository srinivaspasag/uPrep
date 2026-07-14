import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "organization-commons"
  val appVersion      = "1.0-SNAPSHOT"


  val currentDir      = new java.io.File(".").getAbsolutePath()
  val indexOfVedantu  = currentDir.indexOf("vedantu")
  val localRepo       = currentDir.substring(0, indexOfVedantu) + "/vedantu/zowie/local-repo"


  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += Resolver.file("vedantu-local-repo", file(localRepo))(Patterns("[module]/[revision]/[artifact]-[revision].[ext]")),
    sources in doc in Compile := List()
  ).dependsOn(
    commons
  )


  lazy val commons = uri("../../commons")


}
