package com.gu

import sbt._
import Keys._
import java.io.InputStreamReader
import javax.script.{Invocable, ScriptEngineManager}
import io.Source


object SbtJshintPlugin extends Plugin {

  //lazy val distPath = SettingKey[File]("jshint-options", "Path to file containing the jshint options")
  lazy val jsFiles = SettingKey[PathFinder]("jsFiles", "the files to run jshint against")
  lazy val jshint = TaskKey[String]("jshint", "Run jshint code analysis")

  def jshintTask = (jsFiles, streams) map { (files, s) =>
    s.log.info("running jshint...")

//    val js = new ScriptEngineManager().getEngineByName("JavaScript")
    val js = new ScriptEngineManager().getEngineByName("JavaScript")

    js.eval(bundledScript("jshint.js"))
    js.eval(bundledScript("defaultOptions.js"))
    js.eval(bundledScript("getErrors.js"))

    val jscall = js.asInstanceOf[Invocable]

    files.get.map { jsfile =>
      s.log.info("checking " + jsfile.getName)
      val foo = jscall.invokeFunction("getErrors", readFile(jsfile))

      s.log.info("result was:" + foo)
    }

    "complete"
  }

  def bundledScript(fileName: String) = {
    val cl = this.getClass.getClassLoader
    val is = cl.getResourceAsStream(fileName)

    new InputStreamReader(is)
  }

  def readFile(file: File) = {

    val source = Source.fromFile(file, "UTF-8")
    val contents = source.mkString
    source.close

    "alert('foo');\n"
  }

  val jshintSettings: Seq[Project.Setting[_]] = Seq(
    jshint <<= jshintTask
  )
}
