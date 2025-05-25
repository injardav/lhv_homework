package com.example.lhv_homework.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class NameMatcher {
    
    private static final JaroWinklerDistance JWD = new JaroWinklerDistance();
    private static final LevenshteinDistance LD = new LevenshteinDistance();

    public static double jaroWinklerSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return 0.0;
        }
        return 1.0 - JWD.apply(name1, name2);
    }

    public static double jaccardSimilarity(String name1, String name2) {
        String concatName1 = name1.replaceAll("\\s+", "");
        String concatName2 = name2.replaceAll("\\s+", "");
        if (concatName1 == null || concatName2 == null || concatName1.length() < 2 || concatName2.length() < 2) return 0.0;

        Set<String> bigrams1 = getBigrams(concatName1);
        Set<String> bigrams2 = getBigrams(concatName2);

        Set<String> intersection = new HashSet<>(bigrams1);
        intersection.retainAll(bigrams2);

        Set<String> union = new HashSet<>(bigrams1);
        union.addAll(bigrams2);

        return (double) intersection.size() / union.size();
    }

    private static Set<String> getBigrams(String input) {
        Set<String> bigrams = new HashSet<>();
        for (int i = 0; i < input.length() - 1; i++) {
            bigrams.add(input.substring(i, i + 2));
        }
        return bigrams;
    }

    public static int phoneticMatches(List<String> tokens1, List<String> tokens2) {
        DoubleMetaphone dm = new DoubleMetaphone();
        int matchCount = 0;

        for (String t1 : tokens1) {
            String code1 = dm.doubleMetaphone(t1);
            for (String t2 : tokens2) {
                String code2 = dm.doubleMetaphone(t2);
                if (code1.equals(code2)) {
                    matchCount++;
                    break;
                }
            }
        }
        return matchCount;
    }

    public static int levenshteinDistance(String name1, String name2) {
        if (name1 == null || name2 == null) return Integer.MAX_VALUE;
        return LD.apply(name1, name2);
    }

    public static double normalizedLevenshtein(String a, String b) {
        int dist = levenshteinDistance(a, b);
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) dist / maxLen);
    }
}
