package com.gu

import sbt._
import Keys._
import java.io.InputStreamReader
import io.Source
import org.mozilla.javascript.{ScriptableObject, ContextFactory, Context, Function => JsFunction}
import org.mozilla.javascript.tools.shell.{Global, Main}


object SbtJshintPlugin extends Plugin {


  //lazy val distPath = SettingKey[File]("jshint-options", "Path to file containing the jshint options")
  lazy val jshintFiles = SettingKey[PathFinder]("jshintFiles", "the files to run jshint against")
  lazy val jshint = TaskKey[String]("jshint", "Run jshint code analysis")

  def jshintTask = (jshintFiles, streams) map { (files, s) =>
    s.log.info("running jshint...")

    val jscontext =  new ContextFactory().enterContext()
    val scope = new Global()

    scope.init(jscontext)

    jscontext.evaluateReader(scope, bundledScript("jshint.js"), "jshint.js", 1, null)
    jscontext.evaluateReader(scope, bundledScript("defaultOptions.js"), "defaultOptions.js", 1, null)
    jscontext.evaluateReader(scope, bundledScript("getErrors.js"), "getErrors.js", 1, null)

    val errorCount = files.get.map { jsfile =>
      s.log.info("checking " + jsfile.getName)
      val getErrorFunc = scope.get("getErrors", scope).asInstanceOf[JsFunction]
      val errorsInfile = getErrorFunc.call(jscontext, scope, scope, Array(readFile(jsfile)))
      errorsInfile.asInstanceOf[Double]
    }.sum

    if (errorCount > 0) throw new JshintFailedException(errorCount.toInt)

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

    contents
  }

  val jshintSettings: Seq[Project.Setting[_]] = Seq(
    jshint <<= jshintTask
  )
}

class JshintFailedException(count: Int) extends Exception("JSHINT validation failed with " + count + " errors")
