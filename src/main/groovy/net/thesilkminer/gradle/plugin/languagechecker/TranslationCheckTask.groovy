package net.thesilkminer.gradle.plugin.languagechecker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

class TranslationCheckTask extends DefaultTask {

    @TaskAction
    void run() {
        logger.info('Attempting to check translations!')

        def file = TranslationCheckExtension.langDir as File

        if (file == null || !file.isDirectory()) {
            logger.error('File specified is not a directory.')
            throw new RuntimeException('File specified is not a directory')
        }

        def enUs = null
        final List<File> langFiles = new ArrayList<>()

        for (final File langFile : file.listFiles()) {

            def name = langFile.getName()

            if (name != null && !name.isEmpty()) {

                if (name.equals('en_US.lang'))
                    enUs = langFile
                else
                    langFiles.add(langFile)
            }
        }

        if (enUs == null || langFiles.isEmpty()) {

            logger.error('Either the "en_US.lang" file does not exist or no other files are present.')
            logger.error('Aborting process so that no files are hurt in the process')
            return;
        }

        final Map<String, String> map = fillMap(enUs)

        for (final File f : langFiles) {

            writeFile(f, mapEntries(map, f))
        }

        logger.info('Task execution completed')
    }

    private Map<String, String> fillMap(final File enUs) {

        final Map<String, String> enUsEntries = new LinkedHashMap<>()
        int countBlank = 0
        int countComment = 0

        try {

            final BufferedReader reader = new BufferedReader(new FileReader(enUs))
            def currentLine

            while((currentLine = reader.readLine()) != null) {

                final int[] tmp = matchLines(currentLine, countBlank, countComment, enUsEntries)
                countBlank = tmp[0]
                countComment = tmp[1]
            }

            reader.close()
        }
        catch (final Exception e) {

            if (e.getMessage().contains('language file syntax')) {
                logger.error('Rethrowing wrong syntax exception')
                throw e
            }

            e.printStackTrace()
        }

        return enUsEntries
    }

    private Map<String, String> mapEntries(final Map<String, String> enUsEntries, final File otherLang) {

        final Map<String, String> otherLangEntries = fillMap(otherLang)

        final Map<String, String> newOtherLangMap = new LinkedHashMap<>()

        for (Map.Entry<String, String> enUsEntry : enUsEntries.entrySet()) {

            if (enUsEntry.getKey().startsWith(TranslationCheckExtension.blank)) {

                newOtherLangMap.put(enUsEntry.getKey(), null)
                continue
            }

            if (enUsEntry.getKey().startsWith(TranslationCheckExtension.comment)) {

                newOtherLangMap.put(enUsEntry.getKey(), enUsEntry.getValue())
                continue
            }

            final String translated = otherLangEntries.get(enUsEntry.getKey())

            if (translated == null || translated.isEmpty() || translated.equals(' ')) {

                newOtherLangMap.put(enUsEntry.getKey(), enUsEntry.getValue() + ' '
                        + TranslationCheckExtension.translationNeeded)
                continue
            }

            newOtherLangMap.put(enUsEntry.getKey(), translated)
        }

        return newOtherLangMap
    }

    private int[] matchLines(final String currentLine,
                                    final int blank,
                                    final int comment,
                                    final Map<String, String> entries) {

        logger.info('Matching lines')

        if (currentLine.isEmpty() || currentLine.startsWith(' ')) {
            entries.put(TranslationCheckExtension.blank + blank, null)
            final int newBlank = blank + 1
            return [newBlank, comment]
        }

        if (currentLine.startsWith('#')) {
            entries.put(TranslationCheckExtension.comment + comment, currentLine.substring(1))
            final int newComment = comment + 1
            return [blank, newComment]
        }

        if (currentLine.contains('=')) {
            String[] parts = currentLine.split(Pattern.quote('='))

            if (parts.length < 2) {
                parts = [parts[0], '']
            }

            if (parts.length != 2) {
                for (int i = 1; i < parts.length; ++i) {
                    parts[1] = parts[1] + parts[i]
                }
            }

            if (parts[0].endsWith('='))
                parts[0] = parts[0].substring(0, parts[0].length() - 1)

            if (parts[1].startsWith('='))
                parts[1] = parts[1].substring(1)

            entries.put(parts[0], parts[1])

            return [blank, comment]
        }

        throw new RuntimeException('Error in language file syntax.')
    }

    private void writeFile(final File toWrite, final Map<String, String> entries) {

        logger.info('Writing file')

        if (toWrite.exists()) {
            logger.info('Existent file deleted')
            toWrite.delete()
        }

        try {
            PrintWriter out = new PrintWriter(new FileWriter(toWrite))
            logger.info('Inside try')

            for (Map.Entry<String, String> entry : entries.entrySet()) {

                final String key = entry.getKey()
                final String value = entry.getValue()

                logger.info(key)
                logger.info(value)

                if (key == null)
                    continue

                if (key.startsWith(TranslationCheckExtension.blank)) {
                    out.println()
                    continue
                }

                if (key.startsWith(TranslationCheckExtension.comment)) {
                    out.println('#' + value)
                    continue
                }

                if (value == null)
                    continue

                out.println(String.format("%s=%s", key, value))
            }

            out.close()
        }
        catch (final Exception e) {

            e.printStackTrace()
        }
    }
}
