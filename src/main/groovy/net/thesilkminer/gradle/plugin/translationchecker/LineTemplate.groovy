package net.thesilkminer.gradle.plugin.translationchecker

interface LineTemplate {

    String fill(Map<String, String> translations)
}