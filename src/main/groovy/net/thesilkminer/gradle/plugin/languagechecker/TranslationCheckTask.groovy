package net.thesilkminer.gradle.plugin.languagechecker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import groovy.transform.CompileStatic

import joptsimple.*;

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
trait TranslationCheckBatchJob {
    abstract def log(String log)

    def batchTranslationCheck(File baseDir, String templateFileName) {
        def templateFile
        final List<File> langFiles = []

        for (File langFile : baseDir.listFiles()) {
            if (langFile.getName().equals(templateFileName)) {
              log('Found template file: ' + langFile)
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
            log('Processing lang file: ' + f)
            templateFile.processTranslation(f, f)
        }
    }
}

@CompileStatic
class TranslationCheckTask extends DefaultTask implements TranslationCheckBatchJob {

    @Input
    File langDir

    @Input
    String templateFileName = "en_US.lang"

    def log(String log) {
        logger.info(log)
    }

    @TaskAction
    void run() {
        if (langDir == null || !langDir.isDirectory())
            throw new RuntimeException("Path '${langDir}' is not a directory")

        batchTranslationCheck(langDir, templateFileName)
    }
}

@CompileStatic
class Standalone implements TranslationCheckBatchJob {
    final OptionParser parser
    final OptionSpec<File> baseDirs;
    final OptionSpec<String> template;
    final OptionSpec<String> singleMode;
    final OptionSpec<String> output;
    final OptionSpec<Void> help;

    Standalone() {
        parser = new OptionParser();
        baseDirs = parser.nonOptions("dir").ofType(File.class)
        template = parser.accepts("template").withRequiredArg().defaultsTo("en_US.lang")
        singleMode = parser.accepts("single").withRequiredArg()
        output = parser.accepts("output").availableIf(singleMode).withRequiredArg()
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

        boolean isSingleMode = options.has(singleMode);
        for (File baseDir : options.valuesOf(baseDirs)) {
            println "Starting " + baseDir.getAbsolutePath()
            if (isSingleMode) {
                singleFileTranslationCheck(baseDir, options)
            } else {
                batchTranslationCheck(baseDir, options.valueOf(template))
            }
        }
    }

    def singleFileTranslationCheck(File baseDir, OptionSet options) {
        String templateFileName = options.valueOf(template);
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
        templateProcessor.parseFile(templateFile)

        println "Processing ${inputFile.getAbsolutePath()} to ${outputFile.getAbsolutePath()}"
        templateProcessor.processTranslation(inputFile, outputFile)
    }

    static void main(String... args) {
        new Standalone().execute(args)
    }
}
