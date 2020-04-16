package edu.berkeley.nlp.assignments.assign1.student;

import static org.junit.jupiter.api.Assertions.*;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.sound.midi.MidiDeviceTransmitter;
import org.junit.Assert;
import org.junit.Test;

public class LmFactoryTest {
  @Test
  public void testGetOrder() {
    LmFactory f = new LmFactory();
    List<List<String>> data = new ArrayList<>();
    data.add(new ArrayList<String>(Arrays.asList("a", "b")));
    NgramLanguageModel model = f.newLanguageModel(data);

    Assert.assertEquals(3, model.getOrder());
  }

  @Test
  public void testGetNgramLogProbability() {
    LmFactory f = new LmFactory();
    List<List<String>> data = new ArrayList<>();
    data.add(new ArrayList<String>(Arrays.asList("a", "a", "a", "b")));
    NgramLanguageModel model = f.newLanguageModel(data);
    int[] sentence = new int[6];
    sentence[0] = EnglishWordIndexer.getIndexer().addAndGetIndex(model.START);
    sentence[1] = EnglishWordIndexer.getIndexer().addAndGetIndex("a");
    sentence[2] = EnglishWordIndexer.getIndexer().addAndGetIndex("a");
    sentence[3] = EnglishWordIndexer.getIndexer().addAndGetIndex("a");
    sentence[4] = EnglishWordIndexer.getIndexer().addAndGetIndex("b");
    sentence[5] = EnglishWordIndexer.getIndexer().addAndGetIndex(model.STOP);

    System.out.println("Test uni-gram probability...");
    System.out.println("START: " + Math.exp(model.getNgramLogProbability(sentence, 0, 1)));
    System.out.println("a: " + Math.exp(model.getNgramLogProbability(sentence, 1, 2)));
    System.out.println("a: " + Math.exp(model.getNgramLogProbability(sentence, 2, 3)));
    System.out.println("a: " + Math.exp(model.getNgramLogProbability(sentence, 3, 4)));
    System.out.println("b: " + Math.exp(model.getNgramLogProbability(sentence, 4, 5)));
    System.out.println("STOP: " + Math.exp(model.getNgramLogProbability(sentence, 5, 6)));

    System.out.println("Test bi-gram probability...");
    System.out.println("[START, a]: " + Math.exp(model.getNgramLogProbability(sentence, 0, 2)));
    System.out.println("[a, a]: " + Math.exp(model.getNgramLogProbability(sentence, 1, 3)));
    System.out.println("[a, a]: " + Math.exp(model.getNgramLogProbability(sentence, 2, 4)));
    System.out.println("[a, b]: " + Math.exp(model.getNgramLogProbability(sentence, 3, 5)));
    System.out.println("[a, START]: " + Math.exp(model.getNgramLogProbability(new int[] {sentence[1], sentence[0]}, 0, 2)));
    System.out.println("[a, STOP]: " + Math.exp(model.getNgramLogProbability(new int[] {sentence[1], sentence[5]}, 0, 2)));
    System.out.println("[b, STOP]: " + Math.exp(model.getNgramLogProbability(sentence, 4, 6)));

    System.out.println("Test tri-gram probability...");
    System.out.println("[START, a, a]: " + Math.exp(model.getNgramLogProbability(sentence, 0, 3)));
    System.out.println("[a, a, a]: " + Math.exp(model.getNgramLogProbability(sentence, 1, 4)));
    System.out.println("[a, a, b]: " + Math.exp(model.getNgramLogProbability(sentence, 2, 5)));
    System.out.println("[a, b, STOP]: " + Math.exp(model.getNgramLogProbability(sentence, 3, 6)));
  }
}