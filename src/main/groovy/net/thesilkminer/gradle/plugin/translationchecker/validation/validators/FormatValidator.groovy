package net.thesilkminer.gradle.plugin.translationchecker.validation.validators

import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessageAppender
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validator

import groovy.transform.CompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
class FormatValidator implements Validator {
    // based on java.util.Formatter code
    static final Pattern FORMAT_MATCHER = ~/^(\d+\$)?([-#+ 0,(<]*)?(\d+)?(\.\d+)?[tT]?[a-zA-Z]/

    final Map<String, List<String>> templates = new HashMap<>()

    // TODO positional format (index$)
    static List<String> getFormats(String value, ValidationMessageAppender addMessage) {
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
