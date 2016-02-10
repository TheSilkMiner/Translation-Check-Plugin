package net.thesilkminer.gradle.plugin.languagechecker

import org.gradle.api.Plugin
import org.gradle.api.Project

class TranslationCheckPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        project.extensions.create('translation', TranslationCheckExtension)
        project.tasks.create('translationCheck', TranslationCheckTask)
        project.tasks['jar'].dependsOn('translationCheck')
    }
}
