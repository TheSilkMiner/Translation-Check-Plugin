package net.thesilkminer.gradle.plugin.translationchecker.validation.validators

import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessageAppender
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validator

import groovy.transform.CompileStatic

@CompileStatic
class DuplicateTranslations implements Validator {
    def Set<String> templateKeys = new HashSet<>()

    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        for (String key : keys) {
            def added = !templateKeys.add(key)
            if (added) addMessage(0, "Duplicate key ${key} in template")
        }
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {}
}
