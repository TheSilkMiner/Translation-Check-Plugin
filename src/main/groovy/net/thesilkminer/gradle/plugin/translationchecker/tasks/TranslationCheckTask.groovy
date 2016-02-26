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

    @TaskAction
    void run() {

        if (langDir == null && (modId == null || modId.isEmpty())) {
            throw new RuntimeException('You must specify either a directory or a mod ID')
        }

        if (langDir == null) {
            // Attempting to resolve language directory from mod ID.
            langDir = new File("src/main/resources/assets/${modid}/lang")

            if (langDir.exists() && !langDir.isDirectory()) {
                langDir = null
            }
        }

        if (langDir == null || !langDir.isDirectory()) {
            throw new RuntimeException("Path '${langDir}' is not a directory")
        }

        def templateFile
        final List<File> langFiles = []

        for (File langFile : langDir.listFiles()) {
            if (langFile.getName().equals(templateFileName)) {
                logger.info('Found template file: ' + langFile)
                templateFile = new TranslationFileTemplate()
                templateFile.parseFile(langFile)
            } else if (!langFile.isDirectory()) {
                langFiles.add(langFile)
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
