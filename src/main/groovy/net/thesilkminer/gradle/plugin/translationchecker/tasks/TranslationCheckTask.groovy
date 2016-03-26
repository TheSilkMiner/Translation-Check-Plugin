package net.thesilkminer.gradle.plugin.translationchecker.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import groovy.transform.CompileStatic

import net.thesilkminer.gradle.plugin.translationchecker.TranslationFileTemplate
import net.thesilkminer.gradle.plugin.translationchecker.Validators
import net.thesilkminer.gradle.plugin.translationchecker.TranslationCheckBatchJob

@CompileStatic
@SuppressWarnings("all")
// Why doesn't "unused" work?
class TranslationCheckTask extends DefaultTask implements TranslationCheckBatchJob {

    @Input
    String modId

    @Input
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
        if (langDir == null || !langDir.isDirectory())
            throw new RuntimeException("Path '${langDir}' is not a directory")

        def validators = new HashSet<>()
        validators.addAll(enabledValidators)
        batchTranslationCheck(langDir, templateFileName, needsTranslationMark, validators)
    }
}
