package net.thesilkminer.gradle.plugin.translationchecker.tasks

import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationFileTemplate
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationCheckBatchJob
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationTemplateConfigurator
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validators

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings("all")
// Why doesn't "unused" work?
class TranslationCheckTask extends DefaultTask implements TranslationCheckBatchJob {

    @Input
    @Optional
    String modId

    @Input
    @Optional
    File langDir

    @Input
    String templateFileName = 'en_US.lang'

    @Input
    List<String> excludedFileNames = []

    @Input
    String needsTranslationMark = "## NEEDS TRANSLATION ##"

    @Input
    String[] enabledValidators = Validators.allValidators

    def log(String log) {
        logger.info(log)
    }

    @TaskAction
    void run() {

        if (langDir == null && (modId == null || modId.isEmpty())) {
            throw new RuntimeException('You must specify either a directory or a mod ID')
        }

        if (langDir == null) {
           // Attempting to resolve language directory from mod ID.
            langDir = new File("src/main/resources/assets/${modId}/lang")
        }

        if (langDir == null || !langDir.isDirectory())
            throw new RuntimeException("Path '${langDir}' is not a directory")


        def validators = new HashSet<>()
        validators.addAll(enabledValidators)
        batchTranslationCheck(langDir, templateFileName, excludedFileNames,
        { TranslationFileTemplate templateFile ->
                    templateFile.loadValidators(validators)
                    templateFile.needsTranslationMarker = needsTranslationMark
        } as TranslationTemplateConfigurator)
    }
}
