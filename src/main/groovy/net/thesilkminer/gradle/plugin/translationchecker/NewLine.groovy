package net.thesilkminer.gradle.plugin.translationchecker

enum NewLine {
    SYSTEM(System.getProperty("line.separator")),
    CR("\r"),
    LF("\n"),
    CRLF("\r\n");

    final String value;

    private NewLine(String value) {
        this.value = value;
    }

    public static NewLine unix() {
        LF
    }

    public static NewLine dos() {
        CRLF
    }

    public static NewLine windows() {
        dos()
    }

    public static NewLine from(final String from) {
        NewLine toRet = null
        values().each {
            if (it.value.equals(from)) toRet = it
            if (it.name().equals(from)) toRet = it // Prefer name over value
        }
        if (toRet == null) throw new RuntimeException("Invalid new line ending specified: ${from}")
        toRet
    }
}
