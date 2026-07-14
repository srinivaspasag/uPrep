import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "commons"
  val appVersion      = "1.0-SNAPSHOT"


  val currentDir      = new java.io.File(".").getAbsolutePath()
  val indexOfVedantu  = currentDir.indexOf("vedantu")
  val localRepo       = currentDir.substring(0, indexOfVedantu) + "/vedantu/zowie/local-repo"


  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "com" % "stax" % "1.2.0",
    "com.mongodb" % "mongo" % "2.10.1",
    "com.google.code.morphia" % "morphia" % "0.99",
    "com.google.code.morphia" % "morphia-logging-slf4j" % "0.99",
    "org.apache.commons.configuration" % "commons-configuration" % "1.7",
    "org.apache.commons" % "commons-collections" % "3.2.1",
    "org.apache.commons.io" % "commons-io" % "2.4",
    "org.apache.commons.lang" % "commons-lang" % "2.6",
    "org.apache.commons.cli" % "commons-cli" % "1.1",
    "org.apache.commons.validator" % "commons-validator" % "1.4.0",
    "org.apache.commons.logging" % "commons-logging" % "1.1.1",
    "org.apache.commons.codec" % "commons-codec" % "1.3",
    "org.apache.httpcomponents" % "httpcore" % "4.4.11",
    "org.apache.httpcomponents" % "httpclient" % "4.5.9",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7.3",
    "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7",
    "com.fasterxml.jackson.core" % "jackson-annotations" % "2.6.0",
    "freemarker.template" % "freemarker" % "2.3.18",
    "org.json" % "json" % "1.0",
    "com.google.gson" % "gson" % "2.2.2",
    "org.elasticsearch" % "elasticsearch" % "0.20.6",
    "org.apache.lucene" % "lucene-analyzers" % "3.6.2",
    "org.apache.lucene" % "lucene-core" % "3.6.2",
    "org.apache.lucene" % "lucene-highlighter" % "3.6.2",
    "org.apache.lucene" % "lucene-memory" % "3.6.2",
    "org.apache.lucene" % "lucene-queries" % "3.6.2",
    "org.apache.hadoop" % "hadoop-core" % "1.0.0",
    "org.apache.hbase" % "hbase" % "0.92.0",
    "org.apache.zookeeper" % "zookeeper" % "3.3.2",
    "org.apache.log4j" % "log4j" % "1.2.16",
    "org.apache.batik" % "batik-transcoder" % "1.0",
    "com.softlayer" % "sl-objectstorage" % "1.0",
    "org.restlet" % "org.restlet" % "1.0",
    "org.restlet.ext.json" % "org.restlet.ext.json" % "1.0",
    "org.restlet.ext.httpclient" % "org.restlet.ext.httpclient" % "1.0",
    "com.typesafe" %% "play-plugins-mailer" % "2.1.0",
    "com.xuggle.xuggler" % "xuggle-xuggler" % "5.4",
    "com.amazonaws" % "aws-java-sdk-core" % "1.11.774",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.11.774",
    "org.jsoup" % "jsoup" % "1.6.2",
    "com.sun.mail" % "mail" % "1.4.3",
    "com.sun.star" % "ridl" % "3.0.1",
    "com.sun.star" % "juh" % "3.0.1",
    "com.sun.star" % "unoil" % "3.0.1",
    "com.sun.star" % "jurt" % "3.0.1",
    "org.apache.pdfbox" % "pdfbox" % "1.6.0",
    "com.artofsolving.jodconverter-cli" % "jodconverter-cli" % "2.2.2",
    "com.artofsolving.jodconverter" % "jodconverter" % "2.2.2",
    "org.springframework" % "spring-expression" % "3.0.7.RELEASE",
    "com.vedantu.ei" % "vedantu-ei-api" % "1.0"

  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += Resolver.file("vedantu-local-repo", file(localRepo))(Patterns("[module]/[revision]/[artifact]-[revision].[ext]")),
    resolvers += "Maven Central Server" at "http://insecure.repo1.maven.org/maven2/",
 //   resolvers = Resolver.file("vedantu-local-repo", file(localRepo))(Patterns("[module]/[revision]/[artifact]-[revision].[ext]")) + resolvers, 
    sources in doc in Compile := List()
  )

//  unmanagedJars in Compile <<= baseDirectory map { base =>
//	val localRepo = (base / "../local-repo") 
//	val customJars = localRepo ** ("mongodb-2.10.1.jar" || "morphia-0.98.jar") 
//	customJars.classpath
//  }



}
