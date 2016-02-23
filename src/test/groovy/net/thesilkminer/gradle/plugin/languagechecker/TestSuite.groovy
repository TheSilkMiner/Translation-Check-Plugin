package net.thesilkminer.gradle.plugin.languagechecker

import org.junit.Test

class SubstitutionTest {




    static class Params{
        String template;
        String translation;
        String expected;
    }

    def testSubstitution(Closure setup) {
        def params = new Params();
        //setup.resolveStrategy = Closure.DELEGATE_FIRST
        setup.delegate = params
        setup()

        TranslationFileTemplate tft = new TranslationFileTemplate()
        tft.parseFile(new StringReader(params.template))

        def outputBuffer = new StringWriter()
        tft.processTranslation(new StringReader(params.translation), new BufferedWriter(outputBuffer))

        def output = outputBuffer.toString().replace(System.getProperty("line.separator"), "\n")
        def expected = params.expected

        assert output == expected
    }

    @Test
    void testSingleSubstitution() {
        testSubstitution {
            template = "k=v"
            translation = "k=t"
            expected = "k=t\n"
        }
    }

    @Test
    void testMultipleSubstitutions() {
        testSubstitution {
            template = "k1=v1\nk2=v2"
            translation = "k2=t2\nk1=t1"
            expected = "k1=t1\nk2=t2\n"
        }
    }

    @Test
    void testCommentsAndWhitespaces() {
        testSubstitution {
            template = "#test\n\nk1=v1\n \n#@ \nk2=v2\n"
            translation = "#test\nk2=t2\nk1=t1"
            expected = "#test\n\nk1=t1\n \n#@ \nk2=t2\n"
        }
    }
}