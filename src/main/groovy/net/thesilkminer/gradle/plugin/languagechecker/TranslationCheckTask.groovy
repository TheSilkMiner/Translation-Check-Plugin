package net.thesilkminer.gradle.plugin.languagechecker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

interface LineTemplate {
    String fill(Map<String, String> translations)
}

class TranslationFileTemplate {
    static Pattern ALL_WHITESPACE = Pattern.compile('^\\s*$')

    static def ESCAPES = [
        '\n' : '\\n',
        '\r' : '\\r',
        '\t' : '\\t',
        '\f' : '\\f'
    ]

    String escape(String value) {
        value.collectReplacements { ESCAPES[it.toString()] }
    }

    private List<LineTemplate> templates = []

    def parseFile(File file) {
        file.eachLine('UTF-8') {
            templates.add(parseLine(it))
        }
    }

    def parseLine(String line) {
        if (line.startsWith("#") || line.startsWith("!") || ALL_WHITESPACE.matcher(line).matches()) {
            return { line } as LineTemplate;
        } else {
            def split = line.indexOf("=")
            assert split != -1
            def key = line.substring(0, split).trim()
            def original_translation = escape(line.substring(split + 1))
            return { translations ->
                def translation = translations[key]
                translation
                    ? "${key}=${escape(translation)}" as String
                    : "#${key}=${original_translation} ## NEEDS TRANSLATION ##" as String
            } as LineTemplate
        }
    }

    def processTranslation(File inFile, File outFile) {
        def translations = new Properties()
        inFile.withReader('UTF-8') {
            translations.load(it);
        }

        outFile.withWriter('UTF-8') { output ->
            templates.each { output.writeLine(it.fill(translations)) }
        }
    }
}

class TranslationCheckTask extends DefaultTask {

    @TaskAction
    void run() {
        def langDir = TranslationCheckExtension.langDir as File

        if (langDir == null || !langDir.isDirectory()) {
            throw new RuntimeException("Path '${langDir}' is not a directory")
        }

        def templateFile
        final List<File> langFiles = []

        for (File langFile : langDir.listFiles()) {
            if (langFile.getName().equals(TranslationCheckExtension.templateFile)) {
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
