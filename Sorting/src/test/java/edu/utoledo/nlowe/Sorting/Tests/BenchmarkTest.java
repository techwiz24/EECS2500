package edu.utoledo.nlowe.Sorting.Tests;

import edu.utoledo.nlowe.Sorting.Samples.Benchmarks;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Tests for the benchmark class
 */
public class BenchmarkTest
{

    private final String OUTPUT_REGEX = "# Configuration: \\n" +
            "# \\tWill generate random numbers from \\d+ to \\d+\\n" +
            "# \\tData size ranges from \\d+ to \\d+ in steps of \\d+\\n" +
            "# \\tSlower sorts will have \\d+ rounds to average results for each size\\n" +
            "# \\tFaster sorts will have \\d+ rounds to average results for each size\\n" +
            "# \\tWarming up the JVM over \\d+ rounds for each algorithm\\n" +
            "Warming up\\.\\.\\.\\d+ms" +
            "\\n\\nResults:\\n\\n\\n" +
            "Data Size\\tBubble Comparisons\\tBubble Swaps\\tBubble Time\\tInsertion Comparisons\\t" +
            "Insertion Swaps\\tInsertion Time\\tSelection Comparisons\\tSelection Swaps\\tSelection Time\\t" +
            "Quick Comparisons\\tQuick Swaps\\tQuick Time\\tHibbard Comparisons\\tHibbard Swaps\\t" +
            "Hibbard Time\\tKnuth Comparisons\\tKnuth Swaps\\tKnuth Time\\tPratt Comparisons\\tPratt Swaps\\t" +
            "Pratt Time\\n" +
            "(\\d+(\\t(\\d+)?\\.\\d+){21}\\n){10}\\n\\n" +
            "Benchmarks complete. Total runtime per algorithm:\\n" +
            "\\tBubble: \\d+ms\n" +
            "\\tSelection: \\d+ms\n" +
            "\\tInsertion: \\d+ms\n" +
            "\\tQuick: \\d+ms\n" +
            "\\tHibbard: \\d+ms\n" +
            "\\tKnuth: \\d+ms\n" +
            "\\tPratt: \\d+ms\n";

    private final String HELP_REGEX = "Usage: java -Jar sorting.jar \\[--option\\tvalue\\]\\n" +
            "Options:\\n" +
            "\\t--out-file, -o\\t<string>\\tThe path to save results to\\n" +
            "\\t--delimiter, -d\\t<string>\\tThe delimiter to use when printing results \\(default: Tab\\)\\n" +
            "\\t--gen-min, -m\\t<int>\\tThe minimum bound on the randomly generated data \\(default: 0\\)\\n" +
            "\\t--gen-max, -M\\t<int>\\tThe maximum bound on the randomly generated data \\(default: 9999999\\n" +
            "\\t--initial-size, -i\\t<int>\\tThe initial size of the data to sort \\(default: 100\\)\\n" +
            "\\t--max-size, -mx\\t<int>\\tThe maximum size of the data to sort \\(default: 20000\\)\\n" +
            "\\t--step, -s\\t<int>\\tThe amount to increment the size of the data to sort by \\(default: 100\\)\\n" +
            "\\t--slow-rounds, -r\\t<int>\\tThe number of rounds to average all algorithms by \\(default: 10\\)\\n" +
            "\\t--fast-rounds, -R\\t<int>\\tThe number of additional rounds to average Quick sort and all shell algorithms by \\(default: 100\\)\\n" +
            "\\t--warmup, -w\\t<int>\\tThe number of rounds to run each algorithm before starting the benchmark \\(default: 30000\\)\\n" +
            "\\nFlags:\\n" +
            "\\t--help, -h\\tPrint this dialog";

    private final String OUTPUT_FILE_REGEX = "# Configuration: \\n" +
            "# \\tWill generate random numbers from \\d+ to \\d+\\n" +
            "# \\tData size ranges from \\d+ to \\d+ in steps of \\d+\\n" +
            "# \\tSlower sorts will have \\d+ rounds to average results for each size\\n" +
            "# \\tFaster sorts will have \\d+ rounds to average results for each size\\n" +
            "# \\tWarming up the JVM over \\d+ rounds for each algorithm\\n" +
            "Data Size\\tBubble Comparisons\\tBubble Swaps\\tBubble Time\\tInsertion Comparisons\\t" +
            "Insertion Swaps\\tInsertion Time\\tSelection Comparisons\\tSelection Swaps\\tSelection Time\\t" +
            "Quick Comparisons\\tQuick Swaps\\tQuick Time\\tHibbard Comparisons\\tHibbard Swaps\\t" +
            "Hibbard Time\\tKnuth Comparisons\\tKnuth Swaps\\tKnuth Time\\tPratt Comparisons\\tPratt Swaps\\t" +
            "Pratt Time\\n" +
            "(\\d+(\\t(\\d+)?\\.\\d+){21}\\n)*";

    /**
     * Piotr Gabryanczyk - March 27, 2009
     * <p>
     * A regular expression matcher for hamcrest
     * <p>
     * See https://piotrga.wordpress.com/2009/03/27/hamcrest-regex-matcher/
     */
    class RegexMatcher extends BaseMatcher<String>
    {
        private final Pattern regex;

        public RegexMatcher(String regex)
        {
            this.regex = Pattern.compile(regex);
        }

        public boolean matches(Object o)
        {
            return regex.matcher((String) o).find();
        }

        public void describeTo(Description description)
        {
            description.appendText('"' + regex.pattern() + '"');
        }
    }

    private RegexMatcher matches(String regex)
    {
        return new RegexMatcher(regex);
    }

    @Test
    public void runsBenchmarks()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--max-size", "1000",
                "--fast-rounds", "2",
                "--slow-rounds", "1"
        });

        assertThat(out.toString(), matches(OUTPUT_REGEX));
    }

    @Test
    public void printsHelpWhenAsked()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--help"
        });

        assertThat(out.toString(), matches(HELP_REGEX));
    }

    @Test
    public void printsHelpOnOddArguments()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--slow-rounds", "--fast-rounds", "200"
        });

        assertThat(out.toString(), matches(HELP_REGEX));
    }

    @Test
    public void printsHelpOnInvalidArguments()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--foobar", "asdf"
        });

        assertThat(out.toString(), matches("Unrecognized argument: --foobar\\n" + HELP_REGEX));
    }

    @Test
    public void supportsOutputFile()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--max-size", "1000",
                "--fast-rounds", "2",
                "--slow-rounds", "1",
                "--out-file", "results.tsv"
        });

        assertThat(out.toString(), matches(OUTPUT_REGEX));

        try
        {
            File outputFile = new File("results.tsv");
            outputFile.deleteOnExit();

            assertTrue(outputFile.exists());
            assertTrue(outputFile.isFile());

            String fileOutput = new Scanner(outputFile).useDelimiter("\\Z").next();
            assertThat(fileOutput, matches(OUTPUT_FILE_REGEX));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void respectsRandomBounds()
    {
        Benchmarks.main(new String[]{
                "--max-size", "1000",
                "--fast-rounds", "2",
                "--slow-rounds", "1",
                "--gen-min", "3",
                "--gen-max", "5"
        });

        Integer[] data = Benchmarks.generate(100);

        assertEquals(0, Arrays.stream(data).filter((i) -> i < 3 || i > 5).count());
    }

    @Test
    public void canChangeDelimiter()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--max-size", "1000",
                "--fast-rounds", "2",
                "--slow-rounds", "1",
                "--delimiter", "MY_CUSTOM_DELIMITER"
        });

        assertTrue(out.toString().contains("MY_CUSTOM_DELIMITER"));
    }

    @Test
    public void canChangeInitialSizeAndStep()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--initial-size", "5",
                "--max-size", "25",
                "--step", "5",
                "--fast-rounds", "2",
                "--slow-rounds", "1"
        });

        assertThat(out.toString(), matches(".*5\\t.*\\n10\\t.*\\n15\\t.*\\n20\\t.*\\n25\\t.*"));
    }

    @Test
    public void canChangeWarmup()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Benchmarks.main(new String[]{
                "--initial-size", "5",
                "--max-size", "25",
                "--step", "5",
                "--fast-rounds", "2",
                "--slow-rounds", "1",
                "--warmup", "32"
        });

        assertThat(out.toString(), matches(".*Warming up the JVM over 32 rounds for each algorithm.*"));
    }

    @Test
    public void warnsWhenFastRoundsAreLessThanSlowRounds()
    {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));

        Benchmarks.main(new String[]{
                "--initial-size", "5",
                "--max-size", "25",
                "--step", "5",
                "--fast-rounds", "1",
                "--slow-rounds", "2",
        });

        assertThat(err.toString(), matches("WARNING: The number of rounds for the faster " +
                "sorts is lower than that for the slow sorts! The faster sorts will still " +
                "run as many rounds as the slow sorts!\n"));
    }

    @Test
    public void complainsOnError()
    {
        // Create a test file
        File readOnlyFile = new File("readonlytestfile" + System.currentTimeMillis() + Math.random() + ".tsv");
        try
        {
            if (!readOnlyFile.createNewFile())
            {
                fail();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail();
        }

        // Mark it read only
        if (!readOnlyFile.setReadOnly())
        {
            fail();
        }

        // Delete it when we exit
        readOnlyFile.deleteOnExit();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));

        Benchmarks.main(new String[]{
                "--initial-size", "5",
                "--max-size", "25",
                "--step", "5",
                "--fast-rounds", "1",
                "--slow-rounds", "2",
                "--out-file", readOnlyFile.getName()
        });

        assertThat(err.toString(), matches(".*Encountered an error when writing benchmark results.*"));
    }

    @Test
    public void dataGeneratorSizeCorrect()
    {
        assertEquals(10, Benchmarks.generate(10).length);
        assertEquals(100, Benchmarks.generate(100).length);
        assertEquals(1000, Benchmarks.generate(1000).length);
    }
}
