package net.thesilkminer.gradle.plugin.translationchecker.descriptor

import net.thesilkminer.gradle.plugin.translationchecker.NewLine
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validators

// TODO Validate descriptor
class TaskDescriptor {

    @OptionalParameter String modId
    @OptionalParameter String langDir
    String templateFileName
    List<String> excludedFileNames
    String needsTranslationMark
    List<String> enabledValidators
    boolean dryRun
    NewLine lineEnding

    TaskDescriptor() {
        modId = null
        langDir = null
        templateFileName = 'en_US.lang'
        excludedFileNames = []
        needsTranslationMark = '## NEEDS TRANSLATION ##'
        enabledValidators = Validators.allValidators
        dryRun = false
        lineEnding = NewLine.LF
    }
}
