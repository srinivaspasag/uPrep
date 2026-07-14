import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "event-bus-mgmt"
  val appVersion      = "1.0-SNAPSHOT"


  val currentDir      = new java.io.File(".").getAbsolutePath()
  val indexOfVedantu  = currentDir.indexOf("vedantu")
  val localRepo       = currentDir.substring(0, indexOfVedantu) + "/vedantu/zowie/local-repo"


  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "joda-time" % "joda-time" % "2.8.1"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += Resolver.file("vedantu-local-repo", file(localRepo))(Patterns("[module]/[revision]/[artifact]-[revision].[ext]")),
    resolvers += "Maven Central Server" at "http://insecure.repo1.maven.org/maven2/",
    sources in doc in Compile := List()
  ).dependsOn(
    commMgmt,
    orgMgmt,
    eventCommons,
    contentMgmt,
    cmdsMgmt,
    socialMgmt,
    billingMgmt
  )

  
  lazy  val contentMgmt = uri("../../content/content-mgmt");
  lazy  val commMgmt = uri("../../comm/comm-mgmt");
  lazy  val eventCommons = uri("../event-commons");
  lazy  val orgMgmt= uri("../../organization/organization-mgmt");
  lazy  val cmdsMgmt= uri("../../cmds/cmds-mgmt");
  lazy  val socialMgmt= uri("../../social/social-mgmt");
  lazy  val billingMgmt= uri("../../billing/billing-mgmt");


}
