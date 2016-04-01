package net.thesilkminer.gradle.plugin.translationchecker

import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationCheckBatchJob
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationFileTemplate
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationTemplateConfigurator
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validators

import groovy.transform.CompileStatic

import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.OptionSpec

enum NewLine {
    SYSTEM(System.getProperty("line.separator")),
    CR("\r"),
    LF("\n"),
    CRLF("\r\n"),

    UNIX("\n"),
    DOS("\r\n");

    final String value;

    private NewLine(String value) {
        this.value = value;
    }
}

@CompileStatic
class Standalone implements TranslationCheckBatchJob {
    final OptionParser parser
    final OptionSpec<File> baseDirs
    final OptionSpec<String> template
    final OptionSpec<String> excludedFilenames
    final OptionSpec<String> singleMode
    final OptionSpec<String> output
    final OptionSpec<Void> help
    final OptionSpec<String> validators
    final OptionSpec<String> marker
    final OptionSpec<Void> dryRun
    final OptionSpec<NewLine> lineEnding

    Standalone() {
        parser = new OptionParser()
        baseDirs = parser.nonOptions("dir").ofType(File.class)
        template = parser.accepts("template").withRequiredArg().defaultsTo("en_US.lang")
        excludedFilenames = parser.accepts("exclude").withRequiredArg().ofType(String.class).defaultsTo()
        singleMode = parser.accepts("single").withRequiredArg()
        output = parser.accepts("output").availableIf(singleMode).withRequiredArg()
        validators = parser.accepts("validators").withRequiredArg().ofType(String.class).defaultsTo(Validators.allValidatorsAsString)
        marker = parser.accepts("marker").withRequiredArg().ofType(String.class).defaultsTo("## NEEDS TRANSLATION ##")
        dryRun = parser.accepts("dry-run", "runs without modyfing any files")
        lineEnding = parser.accepts("line-ending").withRequiredArg().ofType(NewLine.class).defaultsTo(NewLine.LF)
        help = parser.accepts("help").forHelp()
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

        def configurator = { TranslationFileTemplate templateFile ->
            Set<String> validatorSet = new HashSet<>(options.valueOf(validators).tokenize(','))
            templateFile.loadValidators(validatorSet)

            templateFile.needsTranslationMarker = options.valueOf(marker)
            templateFile.dryRun = options.has(dryRun)
            templateFile.lineEnding = options.valueOf(lineEnding).value
        } as TranslationTemplateConfigurator

        for (File baseDir : options.valuesOf(baseDirs)) {
            println "Starting " + baseDir.getAbsolutePath()
            if (isSingleMode) {
                singleFileTranslationCheck(baseDir, configurator, options)
            } else {
                batchTranslationCheck(baseDir, options.valueOf(template), options.valuesOf(excludedFilenames).asList(), configurator)
            }
        }
    }

    def singleFileTranslationCheck(File baseDir, TranslationTemplateConfigurator configurator, OptionSet options) {
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
        configurator.configure(templateProcessor)

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