package com.gu

import sbt._
import Keys._
import java.io.InputStreamReader
import io.Source
import org.mozilla.javascript.{ScriptableObject, ContextFactory, Context, Function => JsFunction}
import org.mozilla.javascript.tools.shell.{Global, Main}


object SbtJshintPlugin extends Plugin {

  val jshintConf = config("jshint") hide

  //lazy val distPath = SettingKey[File]("jshint-options", "Path to file containing the jshint options")
  lazy val jshintFiles = SettingKey[Seq[PathFinder]]("jshintFiles", "the files to run jshint against")
  lazy val jshintMercy = SettingKey[Int]("jshintMercy", "the number of errors allowed before the jshint task fails")
  lazy val jshint = TaskKey[Unit]("jshint", "Run jshint code analysis")

  def jshintTask = (jshintFiles, jshintMercy, streams) map { (optFiles, mercy, s) =>
    optFiles.foreach { files =>
      s.log.info("running jshint...")

      val jscontext =  new ContextFactory().enterContext()
      val scope = new Global()

      scope.init(jscontext)

      jscontext.evaluateReader(scope, bundledScript("jshint.js"), "jshint.js", 1, null)
      jscontext.evaluateReader(scope, bundledScript("defaultOptions.js"), "defaultOptions.js", 1, null)
      jscontext.evaluateReader(scope, bundledScript("getErrors.js"), "getErrors.js", 1, null)

      val errorCount = files.get.map { jsfile =>
        val getErrorFunc = scope.get("getErrors", scope).asInstanceOf[JsFunction]
        val errorsInfile = getErrorFunc.call(jscontext, scope, scope, Array(readFile(jsfile), jsfile.getName))
        errorsInfile.asInstanceOf[Double]
      }.sum

      if (errorCount > mercy) throw new JshintFailedException(errorCount.toInt)

    }
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
    jshint <<= jshintTask,
    jshintFiles := Seq(),
    jshintMercy := 0
  )
}

class JshintFailedException(count: Int) extends Exception("JSHINT validation failed with " + count + " errors")
