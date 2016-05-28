package net.thesilkminer.gradle.plugin.translationchecker.validation.validators

import net.thesilkminer.gradle.plugin.translationchecker.validation.ValidationMessageAppender
import net.thesilkminer.gradle.plugin.translationchecker.validation.Validator

import groovy.transform.CompileStatic

@CompileStatic
class McFormatCodes implements Validator {
    static Set<Integer> toCodepoints(String... chs) {
        return new HashSet<>(chs.collect {String ch -> ch.codePointAt(0) })
    }

    static final Set<Integer> allowedCodepoints = toCodepoints(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', // colors
                                                              'A', 'B', 'C', 'D', 'E', 'F',
            'k', 'K', // obfuscated
            'l', 'L', // bold,
            'm', 'M', // strikethrough
            'n', 'N', // underline
            'o', 'O', // italic
            'r', 'R', // reset
            '\u00A7', // escape
    );

    static def validateLine(String value, ValidationMessageAppender addMessage) {
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
