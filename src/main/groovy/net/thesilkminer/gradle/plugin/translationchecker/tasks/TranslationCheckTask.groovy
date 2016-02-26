package net.thesilkminer.gradle.plugin.translationchecker.tasks

import net.thesilkminer.gradle.plugin.translationchecker.TranslationFileTemplate

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

@SuppressWarnings("all")
// Why doesn't "unused" work?
class TranslationCheckTask extends DefaultTask {

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

    @TaskAction
    void run() {

        if (langDir == null && (modId == null || modId.isEmpty())) {
            throw new RuntimeException('You must specify either a directory or a mod ID')
        }

        if (langDir == null) {
            // Attempting to resolve language directory from mod ID.
            langDir = new File("src/main/resources/assets/${modId}/lang")

            if (langDir.exists() && !langDir.isDirectory()) {
                langDir = null
            }
        }

        if (langDir == null || !langDir.isDirectory()) {
            throw new RuntimeException("Path '${langDir}' is not a directory")
        }

        if (excludedFileNames.contains(templateFileName)) {
            throw new RuntimeException('Template file cannot be excluded from the check')
        }

        def templateFile
        final List<File> langFiles = []

        for (File langFile : langDir.listFiles()) {
            if (langFile.name.equals(templateFileName)) {
                logger.info('Found template file: ' + langFile)
                templateFile = new TranslationFileTemplate(this)
                templateFile.parseFile(langFile)
            } else if (!langFile.isDirectory() && !excludedFileNames.contains(langFile.name)) {
                langFiles.add(langFile)
            } else {
                logger.info('Skipping file or directory ' + langFile)
            }
        }

        if (templateFile == null) {
            throw new RuntimeException("Template file ${TranslationCheckExtension.baseLanguage} not found")
        }

        for (File f : langFiles) {
            logger.info('Processing lang file: ' + f)
            templateFile.processTranslation(f, f)
        }
    }
}
