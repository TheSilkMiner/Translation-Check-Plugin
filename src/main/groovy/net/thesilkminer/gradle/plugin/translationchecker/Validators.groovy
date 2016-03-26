package net.thesilkminer.gradle.plugin.translationchecker

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

@CompileStatic
class McFormatCodes implements Validator {
    static Set<Integer> toCodepoints(String... chs) {
        return new HashSet<>(chs.collect {String ch -> ch.codePointAt(0) })
    }

    static final Set<Integer> allowedCodepoints = toCodepoints(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', // colors
        'k', // obfuscated
        'l', // bold,
        'm', // strikethrough
        'n', // underline
        'o', // italic
        'r', // reset
        '\u00A7', // escape
    );

    def validateLine(String value, ValidationMessageAppender addMessage) {
        int i = 0
        char[] chars = value.toCharArray()

        while (i < chars.length) {
            int codepoint = Character.codePointAt(chars, i)
            i += Character.charCount(codepoint);
            if (codepoint == 0xA7) {
                if (i >= chars.length) {
                    addMessage(i, "Format character at end of line")
                    break
                } else {
                    codepoint = Character.codePointAt(chars, i)
                    i += Character.charCount(codepoint);
                    if (!(codepoint in allowedCodepoints))
                        addMessage(i, "Illegal control code")
                }
            }
        }
    }

    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        validateLine(value, addMessage)
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {
        validateLine(value, addMessage)
    }

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
        mc_format : wrap(McFormatCodes.class),
    ]

    static String[] getAllValidators() {
        return VALIDATORS.keySet().toArray(new String[0])
    }

    static Validator create(String id) {
        return VALIDATORS[id].create()
    }
}
