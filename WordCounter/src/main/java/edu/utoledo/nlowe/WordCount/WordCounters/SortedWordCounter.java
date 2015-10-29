package edu.utoledo.nlowe.WordCount.WordCounters;

import edu.utoledo.nlowe.CustomDataTypes.CustomSortedLinkedList;
import edu.utoledo.nlowe.WordCount.Word;
import edu.utoledo.nlowe.WordCount.WordCounter;

import java.util.Iterator;

/**
 * A word counter whose underlying data type is sorted alphabetically
 */
public class SortedWordCounter extends WordCounter
{
    /** All encountered words are collected in this list. They are sorted alphabetically */
    CustomSortedLinkedList<Word> words = new CustomSortedLinkedList<>();

    @Override
    public void encounter(String word)
    {
        // Add the word, or if it already exists, increment it
        // Doing it this way saves a few comparisons by not having to search the list twice
        words.addOr(new Word(word), Word::increment);
    }

    @Override
    public long getWordCount()
    {
        long count = 0;

        for (Word w : words)
        {
            count += w.getOccurrenceCount();
        }

        return count;
    }

    @Override
    public long getDistinctWordCount()
    {
        return words.size();
    }

    @Override
    public long getComparisonCount()
    {
        return words.getComparisonCount();
    }

    @Override
    public long getReferenceAssignmentCount()
    {
        return words.getReferenceAssignmentCount();
    }

    @Override
    public Iterator<Word> iterator()
    {
        return words.iterator();
    }
}
