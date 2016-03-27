package net.thesilkminer.gradle.plugin.translationchecker.validation

import groovy.transform.CompileStatic

@CompileStatic
interface Validator {
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage)

    def validateTranslation(String key, String value, ValidationMessageAppender addMessage)
}
