package net.thesilkminer.gradle.plugin.languagechecker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import groovy.transform.CompileStatic

@CompileStatic
interface LineTemplate {
    String fill(Map<String, String> translations)
}

@CompileStatic
class TranslationFileTemplate {
    static ALL_WHITESPACE = ~/^\s*$/

    static Map<String, String> ESCAPES = [
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
        file.eachLine('UTF-8') { templates << parseLine(it) }
    }

    def parseFile(Reader file) {
        file.eachLine { templates << parseLine(it) }
    }

    LineTemplate parseLine(String line) {
        if (line.startsWith("#") || line.startsWith("!") || line ==~ ALL_WHITESPACE) {
            return { line } as LineTemplate;
        } else {
            def split = line.indexOf("=")
            assert split != -1
            def key = line.substring(0, split).trim()
            def original_translation = escape(line.substring(split + 1))
            return {  translations ->
                String translation = translations[key]
                translation
                    ? "${key}=${escape(translation)}" as String
                    : "#${key}=${original_translation} ## NEEDS TRANSLATION ##" as String
            } as LineTemplate
        }
    }

    Map<String, String> parseProperties(Reader reader) {
        def p = new Properties()
        p.load(reader)
        p
    }

    def fillFromTemplate(BufferedWriter output, Map<String, String> translations) {
        templates.each { output.writeLine(it.fill(translations)) }
    }

    def processTranslation(File inFile, File outFile) {
        def translations = inFile.withReader('UTF-8') { parseProperties(it) }
        outFile.withWriter('UTF-8') { fillFromTemplate(it, translations) }
    }

    def processTranslation(Reader reader, BufferedWriter writer) {
        def translations = reader.withCloseable { parseProperties(it) }
        writer.withCloseable { fillFromTemplate(it, translations) }
    }
}

@CompileStatic
class TranslationCheckTask extends DefaultTask {

    @Input
    File langDir

    @Input
    String templateFileName = "en_US.lang"

    @TaskAction
    void run() {
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
            throw new RuntimeException("Template file ${templateFileName} not found")
        }

        for (File f : langFiles) {
            logger.info('Processing lang file: ' + f)
            templateFile.processTranslation(f, f)
        }
    }
}
