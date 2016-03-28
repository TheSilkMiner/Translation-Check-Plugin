package net.thesilkminer.gradle.plugin.translationchecker.validation

import net.thesilkminer.gradle.plugin.translationchecker.validation.validators.DuplicateTranslations
import net.thesilkminer.gradle.plugin.translationchecker.validation.validators.FormatValidator
import net.thesilkminer.gradle.plugin.translationchecker.validation.validators.McFormatCodes
import net.thesilkminer.gradle.plugin.translationchecker.validation.validators.NonEmptyValues
import net.thesilkminer.gradle.plugin.translationchecker.validation.validators.UnusedTranslation

import groovy.transform.CompileStatic

import java.util.regex.Pattern

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
            format : wrap(FormatValidator.class),
    ]

    static String[] getAllValidators() {
        return VALIDATORS.keySet().toArray(new String[0])
    }

    static String getAllValidatorsAsString() {
        return Arrays.toString(allValidators)
    }

    static String[] unWrap(final String validators) {
        String unwrapped = validators
        unwrapped = unwrapped.replace('[', '')
        unwrapped = unwrapped.replace(']', '')

        String[] unwrappedArray = unwrapped.split(Pattern.quote(", "))
        unwrappedArray
    }

    static Validator create(String id) {
        return VALIDATORS[id].create()
    }
}
