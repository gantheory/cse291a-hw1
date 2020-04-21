package edu.berkeley.nlp.assignments.assign1.student;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

//import edu.berkeley.nlp.assignments.assign1.student.MyCountHashMap;

class MyTrigramLm implements NgramLanguageModel {

  // Constants.
  static final double EPSILON = 1e-9;
  static final double LOG_ZERO = Math.log(EPSILON);
  static int BIGRAM_RELATED_CAPACITY = 12000000;
  static int TRIGRAM_RELATED_CAPACITY = 5 * BIGRAM_RELATED_CAPACITY;

  // Absolute discounting.
  static final double d = 0.75;
  static int numOfUniqueWords = 495172;

  // Variables for uni-gram.
  long total;
  long[] wordCounter;
  long[] wordFertilityCounter;
  double[] logUnigramProb;

  // Variables for bi-gram.
  MyCountHashMap bigramCount;
  MyCountHashMap bigramFertilityCount;
  long[] sumOfFertilityCount;
  long[] numOfBigramDiscount;

  // Variables for tri-gram.
  MyCountHashMap trigramCount;
  MyCountHashMap numOfTrigramDiscount;

  public MyTrigramLm(Iterable<List<String>> sentenceCollection) {
    System.out.println("Determining the size of the map...");
    int count = 0;
    for (List<String> sentence : sentenceCollection) {
      ++count;
      if (count > 1000) break;
    }
    if (count <= 1000) {
      BIGRAM_RELATED_CAPACITY = 15000;
      TRIGRAM_RELATED_CAPACITY = 24000;
    }
    bigramCount = new MyCountHashMap(BIGRAM_RELATED_CAPACITY);
    bigramFertilityCount = new MyCountHashMap(BIGRAM_RELATED_CAPACITY);
    trigramCount = new MyCountHashMap(TRIGRAM_RELATED_CAPACITY);
    numOfTrigramDiscount = new MyCountHashMap(BIGRAM_RELATED_CAPACITY);

    System.out.println("Building MyNgramLm...");
    wordCounter = new long[numOfUniqueWords];
    Arrays.fill(wordCounter, 0);

    wordFertilityCounter = new long[numOfUniqueWords];
    Arrays.fill(wordFertilityCounter, 0);

    logUnigramProb = new double[numOfUniqueWords];

    sumOfFertilityCount = new long[numOfUniqueWords];
    Arrays.fill(sumOfFertilityCount, 0);

    numOfBigramDiscount = new long[numOfUniqueWords];
    Arrays.fill(numOfBigramDiscount, 0);

    int sent = 0;
    for (List<String> sentence : sentenceCollection) {
      sent++;
      if (sent % 1000000 == 0)
        System.out.println("On sentence " + sent);
      List<String> stoppedSentence = new ArrayList<String>(sentence);
      stoppedSentence.add(0, NgramLanguageModel.START);
      stoppedSentence.add(NgramLanguageModel.STOP);

      // Compute bi-gram count.
      int prev = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(0));
      ++wordCounter[prev];
      for (int i = 1; i < stoppedSentence.size(); ++i) {
        int word = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(i));
        ++wordCounter[word];
        long hashCode = bigramCount.hashCode(prev, word);
        int originalCount = bigramCount.get(hashCode);
        if (originalCount == 0) {
          ++wordFertilityCounter[word];
        }
        bigramCount.put(hashCode, bigramCount.get(hashCode) + 1);
        prev = word;
      }

      // Compute bi-gram fertility count and tri-gram count.
      int prev1 = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(0));
      int prev2 = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(1));
      for (int i = 2; i < stoppedSentence.size(); ++i) {
        int word = EnglishWordIndexer.getIndexer().addAndGetIndex(stoppedSentence.get(i));
        long hashCode = trigramCount.hashCode(prev1, prev2, word);
        int originalCount = trigramCount.get(hashCode);
        if (originalCount == 0) {
          increaseBigramFertilityCount(prev2, word);

          long hashCodeForDiscount = numOfTrigramDiscount.hashCode(prev1, prev2);
          numOfTrigramDiscount.put(hashCodeForDiscount, numOfTrigramDiscount.get(hashCodeForDiscount) + 1);
        }
        trigramCount.put(hashCode, originalCount + 1);
        prev1 = prev2;
        prev2 = word;
      }
    }

    total = CollectionUtils.sum(wordFertilityCounter);
    for (int i = 0; i < logUnigramProb.length; ++i)
      logUnigramProb[i] = Math.log(wordFertilityCounter[i] / (total + EPSILON) + EPSILON);

    System.out.println("Uni-gram: " + wordCounter.length);
    System.out.println("Bi-gram: " + bigramCount.size());;
    System.out.println("Tri-gram: " + trigramCount.size());;
    System.out.println("Total: " + (wordCounter.length + bigramCount.size() + trigramCount.size()));;
    System.out.println("Done building MyNgramLm.");
  }

  @Override
  public int getOrder() {
    return 3;
  }

  private double getBigramLogProbability(int prev, int word) {
    if (word <0 || word >= numOfUniqueWords) return LOG_ZERO;
    if (prev < 0 || prev >= numOfUniqueWords) return logUnigramProb[word];

    // Discounted probability.
    long hashCode = bigramFertilityCount.hashCode(prev, word);
    int originalCount = bigramFertilityCount.get(hashCode);
    if (sumOfFertilityCount[prev] == 0) return logUnigramProb[word];
    double discountedProb = (Math.max((double)originalCount - d, 0.0)) / (double)sumOfFertilityCount[prev];

    // Alpha.
    double alpha = (double)numOfBigramDiscount[prev] * d / (double)sumOfFertilityCount[prev];

    // Previous ngram probability.
    double previousNgramProb = Math.exp(logUnigramProb[word]);

    return Math.log(discountedProb + alpha * previousNgramProb + EPSILON);
  }

  @Override
  public double getNgramLogProbability(int[] ngram, int from, int to) { // [from, to)
    if (to - from > 3) System.out.println("WARNING: to - from > 3 from MyNgramLm");
    if (to - from == 1) {
      int word = ngram[to - 1];
      return (word < 0 || word >= numOfUniqueWords) ? LOG_ZERO : logUnigramProb[word];
    } else if (to - from == 2) {
      int prev = ngram[to - 2], word = ngram[to - 1];
      return getBigramLogProbability(prev, word);
    } else if (to - from == 3) {
      int prev1 = ngram[to - 3], prev2 = ngram[to - 2], word = ngram[to - 1];
      if (prev1 < 0 || prev1 >= numOfUniqueWords) return getBigramLogProbability(prev2, word);
      if (word < 0 || word >= numOfUniqueWords) return LOG_ZERO;
      if (prev2 < 0 || prev2 >= numOfUniqueWords) return logUnigramProb[word];

      // Discounted probability.
      long hashCode = bigramCount.hashCode(prev1, prev2);
      int sumOfTrigramCount = bigramCount.get(hashCode);

      hashCode = trigramCount.hashCode(prev1, prev2, word);
      int originalCount = trigramCount.get(hashCode);
      if (sumOfTrigramCount == 0) return getBigramLogProbability(prev2, word);

      double discountedProb = (Math.max((double)originalCount - d, 0)) / (double)sumOfTrigramCount;

      // Alpha.
      hashCode = numOfTrigramDiscount.hashCode(prev1, prev2);
      double alpha = (double)numOfTrigramDiscount.get(hashCode) * d / (double)sumOfTrigramCount;

      // Previous ngram probability.
      double previousNgramProb = Math.exp(getBigramLogProbability(prev2, word));

      return Math.log(discountedProb + alpha * previousNgramProb + EPSILON);
    }
    return LOG_ZERO;
  }

  @Override
  public long getCount(int[] ngram) {
    if (ngram.length == 1) {
      int word = ngram[0];
      return word < 0 || word >= numOfUniqueWords ? 0 : wordCounter[word];
    } else if (ngram.length == 2) {
      return bigramCount.get(ngram);
    } else if (ngram.length == 3){
      return trigramCount.get(ngram);
    } else {
      System.out.println("WARNING: length of ngram > 3");
      return 0;
    }
  }

  private void increaseBigramFertilityCount(int prev, int word) {
    long hashCode = bigramFertilityCount.hashCode(prev, word);
    int originalCount = bigramFertilityCount.get(hashCode);
    if (originalCount == 0) ++numOfBigramDiscount[prev];
    bigramFertilityCount.put(hashCode, originalCount + 1);
    ++sumOfFertilityCount[prev];
  }

}

public class LmFactory implements LanguageModelFactory {

  /**
   * Returns a new NgramLanguageModel; this should be an instance of a class that you implement.
   * Please see edu.berkeley.nlp.langmodel.NgramLanguageModel for the interface specification.
   *
   * @param trainingData
   */
  public NgramLanguageModel newLanguageModel(Iterable<List<String>> trainingData) {
    return new MyTrigramLm(trainingData);
  }
}
