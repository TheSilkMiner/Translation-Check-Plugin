package net.thesilkminer.gradle.plugin.translationchecker

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import groovy.transform.CompileStatic

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

@CompileStatic
class FormatValidator implements Validator {
    // based on java.util.Formatter code
    static final Pattern FORMAT_MATCHER = ~/^(\d+\$)?([-#+ 0,(\<]*)?(\d+)?(\.\d+)?[tT]?[a-zA-Z]/

    final Map<String, List<String>> templates = new HashMap<>()

    // TODO positional format (index$)
    List<String> getFormats(String value, ValidationMessageAppender addMessage) {
        List<String> result = new ArrayList<String>()
		int column = 0

		while (!value.isEmpty()) {
			final int nextFormat = value.indexOf("%")
			if (nextFormat == -1) break

			value = value.substring(nextFormat + 1)
			column += nextFormat

			if (value.startsWith("%")) {
				value = value.substring(1)
				column += 1
			} else {
				Matcher match = FORMAT_MATCHER.matcher(value)
				if (!match.find()) {
                    addMessage(column, "Invalid format string")
				} else {
					final int end = match.end()
					result.add(value.substring(0, end))
					value = value.substring(end)
					column += end
				}
			}
		}

		return result;
    }

    @Override
    def validateTemplate(Set<String> keys, String value, ValidationMessageAppender addMessage) {
        List<String> template = getFormats(value, addMessage)
        for (String key : keys)
            templates.put(key, template)
    }

    @Override
    def validateTranslation(String key, String value, ValidationMessageAppender addMessage) {
        List<String> template = templates.get(key)
        if (template != null) {
            List<String> formats = getFormats(value, addMessage)
            if (formats != template) {
                addMessage(0, "Different format order, expected " + template + ", got " + formats);
            }
        }
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
        format : wrap(FormatValidator.class),
    ]

    static String[] getAllValidators() {
        return VALIDATORS.keySet().toArray(new String[0])
    }

    static Validator create(String id) {
        return VALIDATORS[id].create()
    }
}
