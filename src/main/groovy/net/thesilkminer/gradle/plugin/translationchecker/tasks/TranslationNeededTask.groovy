package net.thesilkminer.gradle.plugin.translationchecker.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings("all")
// He, yo, there we go
// another "all" suppress in the code
// I truly hope they'll fix this soon
class TranslationNeededTask extends DefaultTask {

    @Input
    String modId

    @Input
    File langDir

    @Input
    List<String> fileNames = []

    @Input
    String needsTranslationMark = "## NEEDS TRANSLATION ##"

    @TaskAction
    void run() {

        logger.warn('Logger output is INFO. Make sure to run this task with the --info flag')

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

        final List<File> files = []

        if (fileNames.empty) {
            files.addAll(langDir.listFiles())
        } else {
            for (String name : fileNames) {
                files.add(new File(name))
            }
        }

        int total = 0;

        for (File langFile : files) {
            int count = 0;

            langFile.eachLine('UTF-8') {
                if (it.endsWith(needsTranslationMark)) {
                    count++;
                }
            }

            logger.info(count + ' strings that need translation found in file ' + langFile.name)

            total += count;
        }

        logger.warn('A total of ' + total + ' untranslated strings have been found')
    }
}
