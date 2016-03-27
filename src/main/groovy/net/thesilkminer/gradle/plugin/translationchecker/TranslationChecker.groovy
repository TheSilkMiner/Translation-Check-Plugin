package net.thesilkminer.gradle.plugin.translationchecker

import net.thesilkminer.gradle.plugin.translationchecker.tasks.TranslationCheckTask
import net.thesilkminer.gradle.plugin.translationchecker.tasks.TranslationNeededTask

import org.gradle.api.Plugin
import org.gradle.api.Project

import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings("all")
// Once again...
class TranslationChecker implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.extensions.create('translation', PluginExtension)
        project.tasks.create('translationCheck', TranslationCheckTask)
        project.tasks.create('translationNeeded', TranslationNeededTask)

        if (PluginExtension.dependentTaskName.equals("§INTERNAL§")) {
            return
        }

        project.tasks['translationNeeded'].dependsOn('translationCheck')
        project.tasks[PluginExtension.dependentTaskName].dependsOn('translationNeeded')
    }
}
