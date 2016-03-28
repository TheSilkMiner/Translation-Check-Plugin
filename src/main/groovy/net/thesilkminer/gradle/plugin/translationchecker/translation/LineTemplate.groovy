package net.thesilkminer.gradle.plugin.translationchecker.translation

import groovy.transform.CompileStatic

@CompileStatic
interface LineTemplate {
    String fill(Map<String, String> translations)
}
