package net.thesilkminer.gradle.plugin.translationchecker.translation

import groovy.transform.CompileStatic

@CompileStatic
trait TranslationCheckBatchJob {
    abstract def log(String log)

    def batchTranslationCheck(File baseDir, String templateFileName, Collection<String> excludedFileNames, TranslationTemplateConfigurator configurator) {
        final List<File> langFiles = []

        boolean foundTemplate = false
        def templateFile = new TranslationFileTemplate()
        configurator.configure(templateFile)

        for (File langFile : baseDir.listFiles()) {
            if (langFile.name.equals(templateFileName)) {
                log('Found template file: ' + langFile)
                foundTemplate = true
                templateFile.parseTemplate(langFile)
            } else if (!langFile.isDirectory() && !(langFile.name in excludedFileNames)) {
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