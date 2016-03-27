package net.thesilkminer.gradle.plugin.translationchecker.validation

import groovy.transform.CompileStatic

@CompileStatic
interface ValidationMessageAppender {
    def call(int column, String message)
}
