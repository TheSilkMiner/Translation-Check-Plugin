package net.thesilkminer.gradle.plugin.translationchecker.translation

import groovy.transform.CompileStatic

@CompileStatic
interface TranslationTemplateConfigurator {
    def configure(TranslationFileTemplate template);
}