sbt-jshint-plugin
=================

An SBT plugin for running jshint code analysis on .js files in your build.

Getting the plugin
------------------

The most convenient way of using this plugin is to add a source dependency in a scala file under project/project:

```
lazy val plugins = Project("plugins", file("."))
    .dependsOn(uri("git://github.com/guardian/sbt-jshint-plugin.git#1.0"))
```

you will also need to import the plugin's settings in the usual way:

```
seq(jshintSettings : _*)
```


Running the plugin
------------------

Override the jshintFiles setting in your build:

```
jshintFiles <+= sourceDirectory { src =>
 (src / "main"/ "webapp" / "static" / "js" ** "*.js") --- (src / "main"/ "webapp" / "static" / "js" / "lib" ** "*.js")
}
```
you can now run the jshint task which will analyse the files you have configured.


Configuring the plugin
----------------------

The jshint plugin can be configured with the following settings:

* jshintFiles - the files to analyse

* jshintMercy - the number of errors allowed before the jshint task will fail (defaults to 0, showing no mercy)

* jshintOptions - a js file that defines a JSLINT_OPTIONS object that configures which jshint validations to run. If not
set the options defined in src/main/resouces/defaultOptions.js are used.

```
jshintOptions <+= baseDirectory { base =>
  base / "jshintOptions.js"
}
```

Running as part of test
-----------------------

To automatically run the jshint plugin as part of your project's test phase you can add the following to you build.sbt:

```
(test in Test) <<= (test in Test) dependsOn (jshint)
```


Release
-------
To release a new version, tag it and push the tag to github.

```
git tag -a 1.XXX
git push --tags
```



