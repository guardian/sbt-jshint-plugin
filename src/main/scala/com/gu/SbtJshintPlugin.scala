package com.gu

import sbt._
import Keys._
import java.io.InputStreamReader
import io.Source
import org.mozilla.javascript.{ScriptableObject, ContextFactory, Context, Function => JsFunction}


object SbtJshintPlugin extends Plugin {


  //lazy val distPath = SettingKey[File]("jshint-options", "Path to file containing the jshint options")
  lazy val jsFiles = SettingKey[PathFinder]("jsFiles", "the files to run jshint against")
  lazy val jshint = TaskKey[String]("jshint", "Run jshint code analysis")

  def jshintTask = (jsFiles, streams) map { (files, s) =>
    s.log.info("running jshint...")

    val jscontext = ContextFactory.getGlobal.enterContext()
    val scope = jscontext.initStandardObjects

    scope.defineFunctionProperties(Array("print"), JsCallbacks.getClass, ScriptableObject.DONTENUM)

    jscontext.evaluateReader(scope, bundledScript("jshint.js"), "jshint.js", 1, null)
    jscontext.evaluateReader(scope, bundledScript("defaultOptions.js"), "defaultOptions.js", 1, null)
    jscontext.evaluateReader(scope, bundledScript("getErrors.js"), "getErrors.js", 1, null)

    files.get.map { jsfile =>
      s.log.info("checking " + jsfile.getName)
      val getErrorFunc = scope.get("getErrors", scope).asInstanceOf[JsFunction]
      val foo = getErrorFunc.call(jscontext, scope, scope, Array(readFile(jsfile)))
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

    contents
  }

  val jshintSettings: Seq[Project.Setting[_]] = Seq(
    jshint <<= jshintTask
  )
}

object JsCallbacks {

  def print(msg: String) { println(msg) }
}
