package net.thesilkminer.gradle.plugin.translationchecker

import net.thesilkminer.gradle.plugin.translationchecker.bridge.PluginBridge$Groovy
import net.thesilkminer.gradle.plugin.translationchecker.descriptor.TaskDescriptor
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationCheckBatchJob
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationFileTemplate
import net.thesilkminer.gradle.plugin.translationchecker.translation.TranslationTemplateConfigurator

import groovy.transform.CompileStatic

import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.OptionSpec

@CompileStatic
class Standalone implements TranslationCheckBatchJob {
    final OptionParser parser
    /*
    final OptionSpec<File> baseDirs
    final OptionSpec<String> template
    final OptionSpec<String> excludedFilenames
    */
    final OptionSpec<String> singleMode
    final OptionSpec<String> output
    final OptionSpec<Void> help
    /*
    final OptionSpec<String> validators
    final OptionSpec<String> marker
    final OptionSpec<Void> dryRun
    final OptionSpec<NewLine> lineEnding
    */
    final OptionSpec<String> databasePath

    Standalone() {
        parser = new OptionParser()
        databasePath = parser.nonOptions("database-path")/*.withRequiredArg()*/.ofType(String.class)
        /*
        baseDirs = parser.nonOptions("dir").ofType(File.class)
        template = parser.accepts("template").withRequiredArg().defaultsTo("en_US.lang")
        excludedFilenames = parser.accepts("exclude").withRequiredArg().ofType(String.class).defaultsTo()
        */
        singleMode = parser.accepts("single").withRequiredArg()
        output = parser.accepts("output").availableIf(singleMode).withRequiredArg()
        /*
        validators = parser.accepts("validators").withRequiredArg().ofType(String.class).defaultsTo(Validators.allValidatorsAsString)
        marker = parser.accepts("marker").withRequiredArg().ofType(String.class).defaultsTo("## NEEDS TRANSLATION ##")
        dryRun = parser.accepts("dry-run", "runs without modyfing any files")
        lineEnding = parser.accepts("line-ending").withRequiredArg().ofType(NewLine.class).defaultsTo(NewLine.LF)
        */
        help = parser.accepts("help").forHelp()
    }

    def log(String log) {
        println log
    }

    def execute(final String... args) {
        final OptionSet options = parser.parse(args)

        if (options.has(help)) {
            parser.printHelpOn System.out
            return
        }

        if (!options.has(databasePath) || options.valueOf(databasePath) == null || options.valueOf(databasePath).isEmpty()) {
            throw new RuntimeException('You must specify a valid task descriptor path')
        }

        TaskDescriptor descriptor = PluginBridge$Groovy.obtainDescriptor(options.valueOf(databasePath))

        boolean isSingleMode = options.has(singleMode)

        def configurator = { TranslationFileTemplate templateFile ->
            Set<String> validatorSet = new HashSet<>(descriptor.enabledValidators)
            templateFile.loadValidators(validatorSet)

            templateFile.needsTranslationMarker = descriptor.needsTranslationMark
            templateFile.dryRun = descriptor.dryRun
            templateFile.lineEnding = descriptor.lineEnding
        } as TranslationTemplateConfigurator

        final File langDir = new File(descriptor.langDir)

        /*
        for (final File baseDir : new File(descriptor.langDir)) {
            println "Starting " + baseDir.getAbsolutePath()
            if (isSingleMode) {
                singleFileTranslationCheck(baseDir, configurator, options)
            } else {
                batchTranslationCheck(baseDir, options.valueOf(template), options.valuesOf(excludedFilenames).asList(), configurator)
            }
        }
        */

        println "Starting to process directory ${langDir.absolutePath}"
        if (isSingleMode) {
            singleFileTranslationCheck(langDir, configurator, descriptor, options)
        } else {
            batchTranslationCheck(langDir, descriptor.templateFileName, descriptor.excludedFileNames, configurator)
        }
    }

    def singleFileTranslationCheck(final File baseDir,
                                   final TranslationTemplateConfigurator configurator,
                                   final TaskDescriptor desc,
                                   final OptionSet options) {
        String templateFileName = desc.templateFileName
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

        println "Loading template ${templateFile.absolutePath}"
        def templateProcessor = new TranslationFileTemplate()
        configurator.configure(templateProcessor)

        templateProcessor.parseTemplate(templateFile)

        println "Processing ${inputFile.absolutePath} to ${outputFile.absolutePath}"
        templateProcessor.processTranslation(inputFile, outputFile)

        for (def m : templateProcessor.validationMessages.toSorted())
            println m.description()
    }

    static void main(String... args) {
        new Standalone().execute(args)
    }
}