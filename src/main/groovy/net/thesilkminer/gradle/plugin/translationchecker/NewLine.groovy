package net.thesilkminer.gradle.plugin.translationchecker

enum NewLine {
    SYSTEM(System.getProperty("line.separator")),
    CR("\r"),
    LF("\n"),
    CRLF("\r\n"),

    UNIX("\n"),
    DOS("\r\n");

    final String value;

    private NewLine(String value) {
        this.value = value;
    }
}
