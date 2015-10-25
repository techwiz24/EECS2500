package edu.utoledo.nlowe.WordCount.tests;

import edu.utoledo.nlowe.WordCount.WordCounter;
import edu.utoledo.nlowe.WordCount.WordCounters.UnsortedWordCounter;
import edu.utoledo.nlowe.WordCount.sample.Benchmarks;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for edu.utoledo.nlowe.WordCount.WordCounters.UnsortedWordCounter
 */
public class UnsortedWordCounterTest
{

    private InputStream source;
    private UnsortedWordCounter counter;

    @Before
    public void setUp()
    {
        counter = new UnsortedWordCounter();
        source = WordCounter.class.getClassLoader().getResourceAsStream("Hamlet-Scene-1.txt");
    }

    @Test
    public void correctlyCountsWords()
    {
        try
        {
            Benchmarks.runBenchmark(source, counter);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail();
        }

        //TODO: Is this correct?
        assertEquals(1413, counter.getWordCount());
        assertEquals(561, counter.getDistinctWordCount());
    }

}