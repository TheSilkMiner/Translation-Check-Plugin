package net.thesilkminer.gradle.plugin.languagechecker

import org.gradle.api.Plugin
import org.gradle.api.Project

@SuppressWarnings("all")
// Once again...
class LanguageChecker implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.extensions.create('translation', PluginExtension)
        project.tasks.create('translationCheck', TranslationCheckTask)

        if (PluginExtension.dependentTaskName.equals("§INTERNAL§")) {
            return
        }

        project.tasks[PluginExtension.dependentTaskName].dependsOn('translationCheck')
    }
}
