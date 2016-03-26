package net.thesilkminer.gradle.plugin.languagechecker

import groovy.transform.*

@CompileStatic
class NonEmptyValues implements Validator {
    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        if (value == null || value == "") addMessage(0, "Empty template")
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {
        if (value == null || value == "") addMessage(0, "Empty translation")
    }
}

@CompileStatic
class UnusedTranslation implements Validator {
    def Set<String> templateKeys = new HashSet<>()

    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        templateKeys += keys
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {
        if (!(key in templateKeys)) addMessage(0, "Unused key in translation")
    }
}

@CompileStatic
class DuplicateTranslations implements Validator {
    def Set<String> templateKeys = new HashSet<>()

    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        for (String key : keys) {
            def added = !templateKeys.add(key)
            if (added) addMessage(0, "Duplicate key '" + key + "' in template")
        }
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {}
}

interface ValidatorFactory {
    Validator create()
}

@CompileStatic
class Validators {
    static ValidatorFactory wrap(Class<? extends Validator> cls) {
        return { cls.newInstance() } as ValidatorFactory
    }

    static final Map<String, ValidatorFactory> VALIDATORS = [
        non_empty : wrap(NonEmptyValues.class),
        unused : wrap(UnusedTranslation.class),
        duplicate : wrap(DuplicateTranslations.class),
    ]

    static String[] getAllValidators() {
        return VALIDATORS.keySet().toArray(new String[0])
    }

    static Validator create(String id) {
        return VALIDATORS[id].create()
    }
}
