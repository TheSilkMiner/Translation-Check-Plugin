package net.thesilkminer.gradle.plugin.translationchecker.bridge

import net.thesilkminer.gradle.plugin.translationchecker.NewLine
import net.thesilkminer.gradle.plugin.translationchecker.descriptor.TaskDescriptor
import net.thesilkminer.skl.interpreter.api.skd.structure.IDatabase
import net.thesilkminer.skl.interpreter.api.skd.structure.ISkdTag

import groovy.transform.CompileStatic

@CompileStatic
class PluginBridge$Groovy {
    static TaskDescriptor obtainDescriptor(final String filePath) {
        PluginBridge$Java.obtainDescriptor(filePath)
    }

    static void populateDescriptor(final IDatabase database, final TaskDescriptor descriptor) {
        final List<ISkdTag> tags = database.structure().mainTags()
        if (tags.size() > 1) {
            throw new RuntimeException('You are not allowed to specify more than one task specification per file')
        }
        final List<ISkdTag> taskProperties = tags.get(0).children
        if (taskProperties.size() > 1) {
            throw new RuntimeException('At the moment, you are only allowed to specify one property per task')
        }
        final ISkdTag properties = taskProperties.get(0)
        if (properties.properties.get(0).name.equals('type') && properties.properties.get(0).name.equals('external')) {
            throw new RuntimeException('At the moment, properties supplied from external files are not supported')
        }
        final List<ISkdTag> taskParameters = taskProperties.get(0).children
        taskParameters.each {
            // WOW Idea! Now you recognise it as the right type! I'm so proud of you. :P
            final String tagName = it.name
            // Could use a "switch" but nah
            if (tagName.equals('modid')) {
                descriptor.modId = it.properties.get(0).value.get()
            } else if (tagName.equals('langdir')) {
                descriptor.langDir = it.properties.get(0).value.get()
            } else if (tagName.equals('templatefilename')) {
                descriptor.templateFileName = it.properties.get(0).value.get()
            } else if (tagName.equals('needstranslationmark')) {
                descriptor.needsTranslationMark = it.properties.get(0).value.get()
            } else if (tagName.equals('dryrun')) {
                descriptor.dryRun = Boolean.getBoolean(it.properties.get(0).value.get())
            } else if (tagName.equals('lineending')) {
                descriptor.lineEnding = NewLine.from(it.properties.get(0).value.get())
            } else if (tagName.equals('excludedfilenames')) {
                final List<String> fileNames = []
                it.children.each {
                    if (!it.name.equals('file')) {
                        throw new RuntimeException('Only "file" tags are supported inside "excludedfilenames"')
                    }
                    fileNames += [it.properties.get(0).value.get()]
                }
                descriptor.excludedFileNames = fileNames
            } else if (tagName.equals('enabledvalidators')) {
                final List<String> validators = []
                it.children.each {
                    if (!it.name.equals('validator')) {
                        throw new RuntimeException('Only "validator" tags are supported inside "enabledvalidators"')
                    }
                    validators += [it.properties.get(0).value.get()]
                }
            } else {
                throw new RuntimeException("Unrecognized tag: ${it.name}")
            }
        }
    }
}
