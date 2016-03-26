package net.thesilkminer.gradle.plugin.languagechecker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import groovy.transform.*

import joptsimple.*

@CompileStatic
interface LineTemplate {
    String fill(Map<String, String> translations)
}

@Sortable
@Canonical
@CompileStatic
class ValidationMessage {
    String source
    String key
    Integer column
    String message

    String description() {
        "Warning@${source}:${key}:${column} - ${message}"
    }
}

@CompileStatic
interface ValidationMessageAppender {
   def call(int column, String message)
}

@CompileStatic
interface Validator {
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage)
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage)
}

@CompileStatic
class TemplateParseState {
    String source
    List<ValidationMessage> messages

    Set<String> keys = new HashSet<String>()

    Set<String> allKeys(String currentKey) {
        keys.add(currentKey)
        def result = keys
        keys = new HashSet<String>()
        return result
    }

    ValidationMessageAppender createMessageAppender(String key) {
        return { int column, String message ->
            messages << new ValidationMessage(source : source, key : key, column : column, message : message)
        } as ValidationMessageAppender
    }
}

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

    String escape(String value) {
        value.collectReplacements { ESCAPES[it.toString()] }
    }

    public final List<Validator> validators = []

    public final List<ValidationMessage> validationMessages = []

    private List<LineTemplate> templates = []

    def loadValidators(Set<String> ids) {
        Collection<Validator> newValidators = ids.collect Validators.&create
        validators.addAll(newValidators)
    }

    def parseTemplate(File file) {
        def state = new TemplateParseState(source : file.getAbsolutePath(), messages : validationMessages)
        file.eachLine('UTF-8') { templates += parseLine(state, it) }
    }

    def parseTemplate(Reader file) {
        def state = new TemplateParseState(source : "<stream>", messages : validationMessages)
        file.eachLine { templates += parseLine(state, it) }
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
                    if (translation) return "${current_key}=${escape(translation)}" as String
                }

                return "#${current_key}=${original_translation} ## NEEDS TRANSLATION ##" as String
            } as LineTemplate]
        }
    }

    Map<String, String> parseProperties(Reader reader) {
        def p = new Properties()
        p.load(reader)
        p
    }

    def validateTranslation(Map<String, String> translation, String source) {
        translation.each { key, value ->
            ValidationMessageAppender appender = { int column, String message ->
                validationMessages << new ValidationMessage(source : source, key : key, column : column, message : message)
            }

            validators*.validateTranslation(key, value, appender)
        }
    }

    def fillFromTemplate(BufferedWriter output, Map<String, String> translations) {
        templates.each { output.writeLine(it.fill(translations)) }
    }

    def processTranslation(File inFile, File outFile) {
        def translations = inFile.withReader('UTF-8') { parseProperties(it) }
        validateTranslation(translations, outFile.getAbsolutePath())
        outFile.withWriter('UTF-8') { fillFromTemplate(it, translations) }
    }

    def processTranslation(Reader reader, BufferedWriter writer) {
        def translations = reader.withCloseable { parseProperties(it) }
        validateTranslation(translations, "<stream>")
        writer.withCloseable { fillFromTemplate(it, translations) }
    }
}

@CompileStatic
trait TranslationCheckBatchJob {
    abstract def log(String log)

    def batchTranslationCheck(File baseDir, String templateFileName, Set<String> validators) {
        final List<File> langFiles = []

        boolean foundTemplate = false
        def templateFile = new TranslationFileTemplate()
        templateFile.loadValidators(validators)

        for (File langFile : baseDir.listFiles()) {
            if (langFile.getName().equals(templateFileName)) {
              log('Found template file: ' + langFile)
              foundTemplate = true
              templateFile.parseTemplate(langFile)
            } else if (!langFile.isDirectory()) {
                langFiles.add(langFile)
            }
        }

        if (!foundTemplate) {
            throw new RuntimeException("Template file ${templateFileName} not found")
        }

        for (File f : langFiles) {
            log('Processing lang file: ' + f)
            templateFile.processTranslation(f, f)
        }

        for (def m : templateFile.validationMessages.toSorted())
            println m.description()
    }
}

@CompileStatic
class TranslationCheckTask extends DefaultTask implements TranslationCheckBatchJob {

    @Input
    File langDir

    @Input
    String templateFileName = "en_US.lang"

    @Input
    String[] enabledValidators = Validators.allValidators

    def log(String log) {
        logger.info(log)
    }

    @TaskAction
    void run() {
        if (langDir == null || !langDir.isDirectory())
            throw new RuntimeException("Path '${langDir}' is not a directory")

        def ids = new HashSet<>()
        ids.addAll(validators)
        batchTranslationCheck(langDir, templateFileName, ids)
    }
}

@CompileStatic
class Standalone implements TranslationCheckBatchJob {
    final OptionParser parser
    final OptionSpec<File> baseDirs
    final OptionSpec<String> template
    final OptionSpec<String> singleMode
    final OptionSpec<String> output
    final OptionSpec<Void> help
    final OptionSpec<String> validators

    Standalone() {
        parser = new OptionParser()
        baseDirs = parser.nonOptions("dir").ofType(File.class)
        template = parser.accepts("template").withRequiredArg().defaultsTo("en_US.lang")
        singleMode = parser.accepts("single").withRequiredArg()
        output = parser.accepts("output").availableIf(singleMode).withRequiredArg()
        validators = parser.accepts("validators").withRequiredArg().ofType(String.class).defaultsTo(Validators.allValidators)
        help = parser.accepts("h").forHelp()
    }

    def log(String log) {
        println log
    }

    def execute(String... args) {
        OptionSet options = parser.parse(args)

        if (options.has(help)) {
            parser.printHelpOn System.out
            return
        }

        boolean isSingleMode = options.has(singleMode)
        Set<String> validatorSet = new HashSet<>(options.valuesOf(validators))
        for (File baseDir : options.valuesOf(baseDirs)) {
            println "Starting " + baseDir.getAbsolutePath()
            if (isSingleMode) {
                singleFileTranslationCheck(baseDir, validatorSet, options)
            } else {
                batchTranslationCheck(baseDir, options.valueOf(template), validatorSet)
            }
        }
    }

    def singleFileTranslationCheck(File baseDir, Set<String> validators, OptionSet options) {
        String templateFileName = options.valueOf(template)
        File templateFile = new File(baseDir, templateFileName)
        if (!templateFile.isFile())
            throw new RuntimeException("Template file ${templateFile.getAbsolutePath()} not found")

        String inputFileName = options.valueOf(singleMode)
        File inputFile = new File(baseDir, inputFileName)

        if (!inputFile.isFile())
            throw new RuntimeException("Input file ${inputFile.getAbsolutePath()} not found")

        File outputFile
        if (options.has(output)) {
            String outputFileName = options.valueOf(output)
            outputFile = new File(baseDir, outputFileName)
        } else {
            outputFile = inputFile
        }

        println "Loading template ${templateFile.getAbsolutePath()}"
        def templateProcessor = new TranslationFileTemplate()
        templateProcessor.loadValidators(validators)
        templateProcessor.parseTemplate(templateFile)

        println "Processing ${inputFile.getAbsolutePath()} to ${outputFile.getAbsolutePath()}"
        templateProcessor.processTranslation(inputFile, outputFile)

        for (def m : templateProcessor.validationMessages.toSorted())
            println m.description()
    }

    static void main(String... args) {
        new Standalone().execute(args)
    }
}
