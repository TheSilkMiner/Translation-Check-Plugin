package net.thesilkminer.gradle.plugin.languagechecker

interface LineTemplate {

    String fill(Map<String, String> translations)
}