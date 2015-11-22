package edu.utoledo.nlowe.Sorting;

import java.util.ArrayList;

/**
 * Average statistics amongst a collection of sort results
 */
public class AverageSortResult
{
    public final double comparisons;
    public final double swaps;
    public final double time;

    public AverageSortResult(ArrayList<SortResult> results)
    {
        double comparisons = 0;
        double swaps = 0;
        double time = 0;

        for (SortResult result : results)
        {
            comparisons += result.comparisons;
            swaps += result.swaps;
            time += result.time;
        }

        this.comparisons = comparisons/(double)results.size();
        this.swaps = swaps/(double)results.size();
        this.time = time/(double)results.size();
    }
}
