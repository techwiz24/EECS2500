package edu.utoledo.nlowe.Sorting.DeltaGenerator;

import edu.utoledo.nlowe.Sorting.ShellSortDeltaGenerator;

/**
 * A shell sort delta generator that generates the Pratt sequence:
 * <p>
 * All values of P and Q such that 2**P * 3**Q < dataSize
 * <p>
 * Successive iterations are the next smallest value in the sequence
 */
public class PrattSequenceGenerator implements ShellSortDeltaGenerator
{

    /**
     * The pratt sequence for numbers up to <code>Integer.MAX_VALUE</code>
     */
    private final int[] PRATT_SEQUENCE = {
            1, 2, 3, 4, 6, 8, 9, 12, 16, 18, 24, 27, 32, 36, 48, 54, 64, 72, 81, 96, 108, 128, 144, 162, 192,
            216, 243, 256, 288, 324, 384, 432, 486, 512, 576, 648, 729, 768, 864, 972, 1024, 1152, 1296, 1458,
            1536, 1728, 1944, 2048, 2187, 2304, 2592, 2916, 3072, 3456, 3888, 4096, 4374, 4608, 5184, 5832,
            6144, 6561, 6912, 7776, 8192, 8748, 9216, 10368, 11664, 12288, 13122, 13824, 15552, 16384, 17496,
            18432, 19683, 20736, 23328, 24576, 26244, 27648, 31104, 32768, 34992, 36864, 39366, 41472, 46656,
            49152, 52488, 55296, 59049, 62208, 65536, 69984, 73728, 78732, 82944, 93312, 98304, 104976, 110592,
            118098, 124416, 131072, 139968, 147456, 157464, 165888, 177147, 186624, 196608, 209952, 221184,
            236196, 248832, 262144, 279936, 294912, 314928, 331776, 354294, 373248, 393216, 419904, 442368,
            472392, 497664, 524288, 531441, 559872, 589824, 629856, 663552, 708588, 746496, 786432, 839808,
            884736, 944784, 995328, 1048576, 1062882, 1119744, 1179648, 1259712, 1327104, 1417176, 1492992,
            1572864, 1594323, 1679616, 1769472, 1889568, 1990656, 2097152, 2125764, 2239488, 2359296, 2519424,
            2654208, 2834352, 2985984, 3145728, 3188646, 3359232, 3538944, 3779136, 3981312, 4194304, 4251528,
            4478976, 4718592, 4782969, 5038848, 5308416, 5668704, 5971968, 6291456, 6377292, 6718464, 7077888,
            7558272, 7962624, 8388608, 8503056, 8957952, 9437184, 9565938, 10077696, 10616832, 11337408,
            11943936, 12582912, 12754584, 13436928, 14155776, 14348907, 15116544, 15925248, 16777216, 17006112,
            17915904, 18874368, 19131876, 20155392, 21233664, 22674816, 23887872, 25165824, 25509168, 26873856,
            28311552, 28697814, 30233088, 31850496, 33554432, 34012224, 35831808, 37748736, 38263752, 40310784,
            42467328, 43046721, 45349632, 47775744, 50331648, 51018336, 53747712, 56623104, 57395628, 60466176,
            63700992, 67108864, 68024448, 71663616, 75497472, 76527504, 80621568, 84934656, 86093442, 90699264,
            95551488, 100663296, 102036672, 107495424, 113246208, 114791256, 120932352, 127401984, 129140163,
            134217728, 136048896, 143327232, 150994944, 153055008, 161243136, 169869312, 172186884, 181398528,
            191102976, 201326592, 204073344, 214990848, 226492416, 229582512, 241864704, 254803968, 258280326,
            268435456, 272097792, 286654464, 301989888, 306110016, 322486272, 339738624, 344373768, 362797056,
            382205952, 387420489, 402653184, 408146688, 429981696, 452984832, 459165024, 483729408, 509607936,
            516560652, 536870912, 544195584, 573308928, 603979776, 612220032, 644972544, 679477248, 688747536,
            725594112, 764411904, 774840978, 805306368, 816293376, 859963392, 905969664, 918330048, 967458816,
            1019215872, 1033121304, 1073741824, 1088391168, 1146617856, 1162261467, 1207959552, 1224440064,
            1289945088, 1358954496, 1377495072, 1451188224, 1528823808, 1549681956, 1610612736, 1632586752,
            1719926784, 1811939328, 1836660096, 1934917632, 2038431744, 2066242608
    };

    /** The index of the last delta returned */
    private int lastDeltaIndex = -1;

    @Override
    public int generateDelta(int dataSize, int lastDelta)
    {
        if (lastDelta < 0)
        {
            // Locate the first delta for this sequence
            int i = -1;
            do
            {
                i++;
            } while (PRATT_SEQUENCE[i] < dataSize);

            lastDeltaIndex = i - 1;
            return PRATT_SEQUENCE[lastDeltaIndex];
        }
        else if (lastDeltaIndex < 0)
        {
            // We're at the end of the sequence, just return 0 to indicate we're done
            return 0;
        }
        else if (PRATT_SEQUENCE[lastDeltaIndex + 1] == lastDelta)
        {
            // lastDeltaIndex has not changed, return the one we returned the last time
            return PRATT_SEQUENCE[lastDeltaIndex];
        }
        else if (PRATT_SEQUENCE[lastDeltaIndex] == lastDelta)
        {
            // Find the next delta, return zero if we're done
            if (--lastDeltaIndex >= 0)
            {
                return PRATT_SEQUENCE[lastDeltaIndex];
            }
            else
            {
                return 0;
            }
        }
        else
        {
            // This implementation doesn't support jumping to arbitrary delta values
            throw new IllegalStateException("Unable to determine the next delta in the sequence because " +
                    "the last delta value jumped more than expected. Construct a new generator or reset this" +
                    "one by passing a 'lastDelta' of -1 to this method.");
        }
    }
}
