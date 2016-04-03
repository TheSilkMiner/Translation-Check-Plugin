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
}
