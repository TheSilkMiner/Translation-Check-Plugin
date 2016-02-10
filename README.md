# Translation-Check-Plugin
**Status of developing branch (`master`):** [![Build Status](https://travis-ci.org/TheSilkMiner/Translation-Check-Plugin.svg?branch=master)](https://travis-ci.org/TheSilkMiner/Translation-Check-Plugin)

## Description
A simple Gradle task which checks and updates translations.

## Example of usage

To use this task you need to make it visible to script and then configure it with some parameters:

```gradle
buildscript {
  dependencies {
    classpath group: 'net.thesilkminer.gradle.translationchecker',
              name: 'TranslationChecker',
              version: '1.0'
  }
}

task translationCheck(type: net.thesilkminer.gradle.plugin.languagechecker.TranslationCheckTask) {
    langDir = file("src/main/resources/assets/modid/lang")
    //templateFileName = "..." // optional, set to "en_US.lang" by default
}
```

To execute, just call:
```bash
./gradlew translationCheck
```
or something similar, depending on operating system.
