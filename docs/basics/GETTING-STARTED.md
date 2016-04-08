# Getting Started with Translation Checker

With this guide, you will learn how to add this plug-in to your Gradle projects and how to immediately start using it.

## Index
- [Prerequisites](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Prerequisites)
- [Adding the plug-in](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Adding-the-plug-in)
- [Configuring the task](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Configuring-the-task)
  - [Modding community](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Modding-community)
  - [Developers community](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Developers-community)
- [First run](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#First-run)
- [What next]https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#What-next)

## Prerequisites
Before being able to use this plug-in, you must meet certain requirements:
- You must run it under Java 6 or later. Previous versions of Java are not supported and may lead to undocumented behaviour.
- Your project must be using Gradle. Other build managers such as Maven are not currently supported.
- You must have some (basic) Groovy knowledge.

If you meet all of this requirements, then you can proceed to the next session.

## Adding the plug-in
Before using all the new features added by this plug-in, you have to tell Gradle where to find the plug-in and which one you should use.
To do that, you must add the `OpenMods` repository to the `buildscript`.

Add the following code at the beginning of your `build.gradle` file.

```groovy
buildscript {
    repositories {
	    maven {
		    name 'OpenMods Third Party'
			url 'https://repo.openmods.info/artifactory/simple/thirdparty'
		}
	}
}
```

What the piece of code above does is telling Gradle to add the `OpenMods Third Party` maven repository, available at the specified `url`, to
the `buildscript`. This will allow Gradle to search for artifacts in the specified repository.

But now we need to tell Gradle which file it should look for in that repository. We do that by adding a dependency through the keyword
`dependencies` to our `buildscript`.

Add the following code in between the `repository` section and the closing `buildscript` bracket:

```groovy
    dependencies {
	    classpath group: 'net.thesilkminer.gradle.translationchecker',
		          name: 'TranslationChecker',
				  version: '1.1'
	}
```

This piece of code tells Gradle that the current project is dependent on a certain artifact. As a consequence, the artifact should be added to
Java's classpath (hence why the `classpath` keyword). It also specifies which artifact should be added, following maven conventions. For ease
of reading, the three parts which form a Maven artifact name are split into separate fields.

Browsing Artifactory directly on OpenMods's website will show you this artifact position: nested into various sub-directories, each one
corresponding to a part of the group, the name or the version. E.g., the current artifact is stored in the directory
`net/thesilkminer/gradle/translationchecker/TranslationChecker/1.1/`, starting from the repository root.

Now save your `build.gradle` file: you have successfully added the Translation Checker plug-in to your Gradle project. To learn how to configure
the various tasks needed, proceed to the next section.

## Configuring the task
Now that you added the plug-in to Gradle, you must configure the project with the various tasks you would like to use. To do this there are two
ways. The first uses a `plugin` declaration and an extension which tells Translation Checker to automatically set all the dependencies and
task priorities. Since this way requires a more advanced knowledge, it won't be treated here. Instead you will set up a task which you will call
later in this guide. So let's get started!

To create a task, you need to declare the task name and the type of task. This plug-in offers two types of tasks: an updater and a simple checker.
Generally speaking, you would want to use the updater. To do this you have to edit once again your `build.gradle` file. Make sure you have already
added the plug-in to the `buildscript`, as depicted in the previous section, then add this at the end of your file:

```groovy
task translationCheck(type: net.thesilkminer.gradle.plugin.translationchecker.tasks.TranslationCheckTask) {
    // Parameters here
}
```

This tells Gradle to add a task with the specified name (in this case `translationCheck`) of the specified type. This way you will be able to run
this task by calling it through PowerShell, Terminal or Bash with the command

```posh
./gradlew translationCheck
```

At the moment, though, the task does not work because it misses certain required parameters. To specify them you will have to add them to the body
of the task, replacing the comment we added before. In other words, you will have to replace the text `// Parameters here` with the values.

First of all, this plug-in was developed for use within the Minecraft Modding community, so it is obvious it has some advantages for that
environment. So, if you are developing a mod, follow the instructions in the
[Modding community](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Modding-community)
section. Otherwise, follow the instructions in the 
[Developers community](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Developers-community)
section.

### Modding community
First of all, these instructions will apply to your project **if and only if** you didn't change the default project structure. In other words,
if your `.lang` files are located in `src/main/resources/assets/${modId}/lang/**`. If this is not the case, then please follow the instructions
in the [Developers community](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#Developers-community)
section.

To configure the plug-in to check all your language files simply add the following couple as parameters in your task. Obviously replace `yourModId`
with the ID of your mod.

```groovy
    modId = yourModId
```

Now proceed to the next section
([First run](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/GETTING-STARTED.md#First-run)), to run the plug-in for
the first time!

### Developers community
To configure the task to correctly check all the various language files, you will have to specify two parameters. The first one (`langDir`) is the
directory where all the language files are located; the second one (`templateFileName`) is the name of the template file, which will be used by the
plug-in to construct and update all the other language files.

To configure the plug-in to check the specified directory, you need to pair the name `langDir` to a `file` property. To do this in Groovy, you can
specify the directory path in between `file(` and `)`, much like a method call. The directory path you should specify is relative to `build.gradle`.
You can also specify an absolute path, but it will limit portability (especially with Git repositories) and could lead to undocumented behaviour.
Also, you should use only forward slashes as separators (`/`). Back-slashes (`\`) are not supported by all operative system, so its usage is highly
discouraged.

Then you need to tell Gradle which file is the main one, the one that will always have the most updated translations and structure. To do this you
need to pair the `templateFileName` property with a fully-qualified file name, which means both name and extension. Specifying the path is not
needed. If your template file's name is equal to `en_US.lang` (case-sensitive), you can avoid specifying it. This because that is the default value
of the variable.

At the end of the process, you should add the following parameters to the task. Obviously you need to replace the various values as specified before.

```groovy
    langDir = file(path/to/your/directory)
    templateFileName = 'name.ext'
```

Save your `build.gradle` and then you can proceed to the next section!

## First run
You have reached this point, after having configured the plug-in to suit your needs. It is about time to test if all the values are correctly specified.
The only way to do this is by running the task.

Open your command line (e.g. Bash, Terminal or PowerShell) and type in the following command:

```posh
./gradlew translationCheck
```

This tells Gradle to run the specified task, which we just created. While Gradle runs you should see some lines appearing on the console. When the
process has finished, a `BUILD SUCCESSFUL` string will be printed out. If, for whatever reason, you should see `BUILD FAILED`, make sure to check your
configuration.

Now all your translations have been updated, reordered and any new addition or removal has been ported from the template to all the other files. All
the entries have also been validated to make sure they did not contain any errors. You can check this by yourself navigating to the path you specified
and checking all the files. Missing translations are indicated through the suffix `### NEEDS TRANSLATION ###` and their lines are commented out.

Congratulations! You have completed the Getting Started guide. Do you want to keep going? Then visit the next section!

## What next?
You could learn how to [customize your task even more](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/basics/CUSTOMIZATION.md),
or you could checkout our [full documentation](https://github.com/TheSilkMiner/Translation-Check-Plugin/blob/master/docs/full/INDEX.md) to discover all
the capabilities of this plug-in.
