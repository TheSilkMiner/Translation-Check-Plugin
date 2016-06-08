package net.thesilkminer.gradle.plugin.translationchecker.tasks

import net.thesilkminer.gradle.plugin.translationchecker.bridge.PluginBridge$Groovy
import net.thesilkminer.gradle.plugin.translationchecker.descriptor.TaskDescriptor
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationFileTemplate
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationCheckBatchJob
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationTemplateConfigurator

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

import groovy.transform.CompileStatic

@CompileStatic
class TranslationCheckTask extends DefaultTask implements TranslationCheckBatchJob {

    @Input
    String databaseLocation

    def log(String log) {
        logger.info(log)
    }

    @TaskAction
    @SuppressWarnings("all")
    void run() {

        if (databaseLocation == null || databaseLocation.isEmpty()) {
            throw new RuntimeException('You must specify the path to the task descriptor file')
        }

        TaskDescriptor descriptor = PluginBridge$Groovy.obtainDescriptor(databaseLocation)

        File langDir

        if (descriptor.langDir == null && (descriptor.modId == null || descriptor.modId.isEmpty())) {
            throw new RuntimeException('You must specify either a directory or a mod ID')
        }

        if (descriptor.langDir == null) {
           // Attempting to resolve language directory from mod ID.
            langDir = new File("src/main/resources/assets/${descriptor.modId}/lang")
        } else {
            langDir = new File(descriptor.langDir)
        }

        if (langDir == null || !langDir.isDirectory())
            throw new RuntimeException("Path '${langDir}' is not a directory")


        def validators = new HashSet<>()
        validators.addAll(descriptor.enabledValidators)
        batchTranslationCheck(langDir, descriptor.templateFileName, descriptor.excludedFileNames,
        { TranslationFileTemplate templateFile ->
                    templateFile.loadValidators(validators)
                    templateFile.needsTranslationMarker = descriptor.needsTranslationMark
                    templateFile.dryRun = descriptor.dryRun
                    templateFile.lineEnding = descriptor.lineEnding
        } as TranslationTemplateConfigurator)
    }
}
