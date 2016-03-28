package net.thesilkminer.gradle.plugin.translationchecker.validation.validators

import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessageAppender
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validator

import groovy.transform.CompileStatic

@CompileStatic
class NonEmptyValues implements Validator {
    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        if (value == null || value == "") addMessage(0, 'Empty template')
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {
        if (value == null || value == "") addMessage(0,'Empty translation')
    }
}
