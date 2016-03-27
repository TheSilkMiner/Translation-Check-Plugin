package net.thesilkminer.gradle.plugin.translationchecker.validation.validators

import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessageAppender
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validator

import groovy.transform.CompileStatic

@CompileStatic
class UnusedTranslation implements Validator {
    def Set<String> templateKeys = new HashSet<>()

    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        templateKeys += keys
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {
        if (!(key in templateKeys)) addMessage(0, 'Unused key in translation')
    }
}
