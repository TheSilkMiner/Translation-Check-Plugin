package net.thesilkminer.gradle.plugin.translationchecker.translation

import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessage
import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessageAppender

import groovy.transform.CompileStatic

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