# Translation-Check-Plugin
**Status of developing branch (`master`):** [![Build Status](https://travis-ci.org/TheSilkMiner/Translation-Check-Plugin.svg?branch=master)](https://travis-ci.org/TheSilkMiner/Translation-Check-Plugin)

## Description
A simple Gradle task/plugin which checks and updates translations.

## Example of usage
To use this task you need to make it visible to script and then configure it with some parameters:

```gradle
buildscript {
  repositories {
    maven {
	  name 'OpenMods Third Party'
	  url 'http://repo.openmods.info/artifactory/simple/thirdparty'
	}
  }
  dependencies {
    classpath group: 'net.thesilkminer.gradle.translationchecker',
              name: 'TranslationChecker',
              version: '1.1'
  }
}

task translationCheck(type: net.thesilkminer.gradle.plugin.translationchecker.tasks.TranslationCheckTask) {
    //langDir = file("src/main/resources/assets/modid/lang") // Resolved by default
	modId = "..."
    //templateFileName = "..." // optional, set to "en_US.lang" by default
    // See documentation for more options
}
```

To execute, just call:
```posh
./gradlew translationCheck
```
in Terminal or PowerShell.

For more information, refer to the documentation, available in the `docs` directory.
