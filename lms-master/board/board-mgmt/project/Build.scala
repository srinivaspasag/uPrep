import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "board-mgmt"
  val appVersion      = "1.0-SNAPSHOT"


  val currentDir      = new java.io.File(".").getAbsolutePath()
  val indexOfVedantu  = currentDir.indexOf("vedantu")
  val localRepo       = currentDir.substring(0, indexOfVedantu) + "/vedantu/zowie/local-repo"


  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "org.dom4j" % "dom4j" % "1.6.1",
    "javax.xml" % "stax-api" % "1.0.1",
    "org.apache.xmlbeans" % "xmlbeans" % "2.3.0",
    "org.apache.poi" % "poi" % "3.9-20121203",
    "org.apache.poi" % "poi-excelant" % "3.9-20121203",
    "org.apache.poi" % "poi-ooxml" % "3.9-20121203",
    "org.apache.poi" % "poi-ooxml-schemas" % "3.9-20121203"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += Resolver.file("vedantu-local-repo", file(localRepo))(Patterns("[module]/[revision]/[artifact]-[revision].[ext]")),
    sources in doc in Compile := List()
  ).dependsOn(
    boardCommons
  )


  lazy val boardCommons = uri("../../board/board-commons")


}
