package net.thesilkminer.gradle.plugin.translationchecker

import net.thesilkminer.gradle.plugin.translationchecker.tasks.TranslationCheckTask

class TranslationFileTemplate {

    TranslationCheckTask task

    TranslationFileTemplate(TranslationCheckTask t) {
        task = t
    }

    static ALL_WHITESPACE = ~/^\s*$/

    static Map<String, String> ESCAPES = [
            '\n' : '\\n',
            '\r' : '\\r',
            '\t' : '\\t',
            '\f' : '\\f'
    ]

    static String escape(String value) {
        value.collectReplacements { ESCAPES[it.toString()] }
    }

    private List<LineTemplate> templates = []

    def parseFile(File file) {
        file.eachLine('UTF-8') {
            templates << parseLine(it)
        }
    }

    static  LineTemplate parseLine(String line) {
        if (line.startsWith("#") || line.startsWith("!") || line ==~ ALL_WHITESPACE) {
            return { line };
        } else {
            def split = line.indexOf("=")
            assert split != -1
            def key = line.substring(0, split).trim()
            def original_translation = escape(line.substring(split + 1))
            return { translations ->
                def translation = translations[key]
                translation ?
                        "${key}=${escape(translation)}" as String :
                        "#${key}=${original_translation} ${task.needsTranslationMark}" as String
            }
        }
    }

    def processTranslation(File inFile, File outFile) {
        def translations = new Properties()
        inFile.withReader('UTF-8') {
            translations.load(it);
        }

        outFile.withWriter('UTF-8') { output ->
            templates.each { output.writeLine(it.fill(translations as Map<String, String>)) }
        }
    }
}
