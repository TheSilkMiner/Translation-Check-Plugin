package net.thesilkminer.gradle.plugin.translationchecker.translation

import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessage
import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessageAppender
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validator
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validators

import groovy.transform.CompileStatic

@CompileStatic
class TranslationFileTemplate {

    static final String PREFIX_ALIAS = "#@alias "
    static final ALL_WHITESPACE = ~/^\s*$/
    static final Map<String, String> ESCAPES = [
        '\n' : '\\n',
        '\r' : '\\r',
        '\t' : '\\t',
        '\f' : '\\f'
    ]

    static String escape(String value) {
        value.collectReplacements { ESCAPES[it.toString()] }
    }

    public final List<Validator> validators = []

    public final List<ValidationMessage> validationMessages = []

    public String needsTranslationMarker =  "## NEEDS TRANSLATION ##"

    private List<LineTemplate> templates = []

    def loadValidators(Set<String> ids) {
        Collection<Validator> newValidators = ids.collect Validators.&create
        validators.addAll(newValidators)
    }

    def parseTemplate(File file) {
        def state = new TemplateParseState(source : file.getAbsolutePath(), messages : validationMessages)
        file.eachLine('UTF-8') {
            String line = it // Needed because of IntelliJ compile-time error
            templates += parseLine(state, line)
        }
    }

    def parseTemplate(Reader file) {
        def state = new TemplateParseState(source : "<stream>", messages : validationMessages)
        file.eachLine {
            String line = it // Needed because of IntelliJ compile-time error
            templates += parseLine(state, line)
        }
    }

    List<LineTemplate> parseLine(TemplateParseState state, String line) {
        if (line.startsWith("#") || line.startsWith("!") || line ==~ ALL_WHITESPACE) {
            if (line.startsWith(PREFIX_ALIAS)) {
                def key = line.substring(PREFIX_ALIAS.size())
                state.keys.add(key)
                return []
            }
            return [{ line } as LineTemplate]
        } else {
            def split = line.indexOf("=")
            assert split != -1
            def current_key = line.substring(0, split).trim()
            def keys = state.allKeys(current_key)
            def original_translation = escape(line.substring(split + 1))

            def messageAppender = state.createMessageAppender(current_key)
            validators*.validateTemplate(keys, original_translation, messageAppender)

            return [{ translations ->
                for (key in keys) {
                    String translation = translations[key]
                    if (translation) return "${current_key}=${escape((String) translation)}" as String
                }

                return "#${current_key}=${original_translation} ${needsTranslationMarker}" as String
            } as LineTemplate]
        }
    }

    static Map<Object, Object> parseProperties(Reader reader) {
        def p = new Properties()
        p.load(reader)
        p
    }

    def validateTranslation(Map<Object, Object> translation, String source) {
        translation.each { key, value ->
            ValidationMessageAppender appender = { int column, String message ->
                validationMessages << new ValidationMessage(source : source, key : key, column : column, message : message)
            }

            validators*.validateTranslation((String) key, (String) value, appender)
        }
    }

    def fillFromTemplate(BufferedWriter output, Map<Object, Object> translations) {
        templates.each { output.writeLine(it.fill(translations)) }
    }

    def processTranslation(File inFile, File outFile) {
        def translations = inFile.withReader('UTF-8') { parseProperties(it) }
        validateTranslation(translations, outFile.getAbsolutePath())
        outFile.withWriter('UTF-8') { fillFromTemplate(it, translations) }
    }

    def processTranslation(Reader reader, BufferedWriter writer) {
        def translations = reader.withCloseable { parseProperties((Reader) it) }
        validateTranslation(translations, "<stream>")
        writer.withCloseable { fillFromTemplate((BufferedWriter) it, translations) }
    }
}

