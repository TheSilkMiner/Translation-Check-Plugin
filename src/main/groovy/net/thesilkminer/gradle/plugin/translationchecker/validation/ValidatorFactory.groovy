package net.thesilkminer.gradle.plugin.translationchecker.validation

import groovy.transform.CompileStatic

@CompileStatic
interface ValidatorFactory {
    Validator create()
}