/*
Input: Email message
Output:
A) Bag of words, ordered by index in the vector
B) For each email, outputs a vector of
1. Length of email (bytes)
2. Length of email (words)
3. Number of '?'
4. Number of questioning words (like "Who Why How When Where")
5. Number of "formal" words. (like "Sir", "Yours sincerely")
6. Bag of words, listed in order of the bag of words given.
7. Number of replies this email has

Other things we can do:
Metadata features:
1. If replied to sender earlier
2. If sender sent email but user ignored.
3. Sender email address. Internal email, external, from .com?
4. Many people in CC? Blasted email

*/
package com.expee.ml.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GetFeature {
  private static final int MIN_COUNT = 10;
  public static void makeEmailSetFeatures(Set<Email> emails, String OUTPUT) throws IOException {
    File fold = new File(OUTPUT);
    fold.delete();
    File file = new File(OUTPUT);
    file.createNewFile();
    FileWriter filewriter = new FileWriter(file,true);
    BufferedWriter writer = new BufferedWriter(filewriter);
    Map<String, Integer> wordCount = new HashMap<String, Integer>();
    for (Email email: emails) {
      String msg = email.getText();
      String[] wordArray = msg.split("\\s");
      for (String word: wordArray) {
        String strippedLowerWord = word.replaceAll("[^\\w]","").toLowerCase();
        if (wordCount.containsKey(strippedLowerWord)) {
          wordCount.put(strippedLowerWord, wordCount.get(strippedLowerWord)+1);
        }
        else {
          wordCount.put(strippedLowerWord, 1);
        }
      }
    }
    Map<String, Integer> wordMap = new HashMap<String, Integer>();
    int idx = 0;
    writer.write("Length of email by bytes, Length of email by words, Number of question marks, Number of question words, Number of formal words, ");
    for (Map.Entry<String, Integer> entry: wordCount.entrySet()) {
      if (entry.getValue() >= MIN_COUNT) {
        writer.write(entry.getKey() + ", ");
        wordMap.put(entry.getKey(), idx);
        idx++;
      }
    }
    writer.write(" Number of replies this email has\n");
    for (Email email : emails) {
      GetFeature.printEmailFeatures(wordMap, writer, email);
    }
  }
  public static void printEmailFeatures(Map<String, Integer> wordMap, BufferedWriter writer, Email email)  throws IOException {
    String[] questionWordArray = {"Could", "Would", "Who", "When", "Where", "What", "Why", "How", "Is", "Are", "Will", "May", "Might"};
    String[] formalWordArray = {"Yours", "Sincerely", "Sir", "Regards"};
    HashSet<String> questionWordSet = new HashSet<String>();
    for (String word : questionWordArray) {
      questionWordSet.add(word);
    }
    HashSet<String> formalWordSet = new HashSet<String>();
    for (String word : formalWordArray) {
      formalWordSet.add(word);
    }
    String msg = email.getText();
    String[] wordArray = msg.split("\\s");
    int numWords = wordArray.length;
    int numQuestionMarks = 0;
    for (int i = 0; i < msg.length(); i++) {
      if (msg.charAt(i) == '?') {
        numQuestionMarks++;
      }
    }
    int numQuestionWords = 0;
    int numFormalWords = 0;
    int[] bagOfWords = new int[wordMap.size()];
    for (int i = 0; i < bagOfWords.length; i++) {
      bagOfWords[i] = 0;
    }
    for (String word : wordArray) {
      String strippedWord = word.replaceAll("[^\\w]","");
      if (word.length() == 0) continue;
      String strippedLowerWord = strippedWord.toLowerCase();
      if (questionWordSet.contains(strippedWord)) {
        numQuestionWords++;
      }
      if (formalWordSet.contains(strippedWord)) {
        numFormalWords++;
      }
      if (wordMap.containsKey(strippedLowerWord)) {
        bagOfWords[wordMap.get(strippedLowerWord)]++;
      }
    }
    //Message length (bytes)
    writer.write(msg.length() + ", ");
    // Message length (words)
    writer.write(numWords + ", ");
    // Number of question marks
    writer.write(numQuestionMarks + ", ");
    // Number of formal words:
    writer.write(numFormalWords + ", ");
    // Number of iterrogative words
    writer.write(numQuestionWords + ", ");
    // Bag of words
    for (int i = 0; i < bagOfWords.length; i++) {
      writer.write(bagOfWords[i] + ", ");
    }
    // Number of replies this email has
    writer.write(String.valueOf(email.getChildren().size()));
    writer.write("\n");
  }
}