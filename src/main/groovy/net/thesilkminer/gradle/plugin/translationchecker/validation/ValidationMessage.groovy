package net.thesilkminer.gradle.plugin.translationchecker.validation

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.Sortable

@Sortable
@Canonical
@CompileStatic
class ValidationMessage {
    String source
    String key
    Integer column
    String message

    String description() {
        "Warning@${source}:${key}:${column} - ${message}"
    }
}
