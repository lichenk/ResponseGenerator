/*
Input: Email message
Output: Features, which are:
1. Whether this email has a reply
2. Length of email (bytes)
3. Length of email (words)
4. Number of '?'
5. Number of questioning words (like "Who Why How When Where")
6. Number of "formal" words. (like "Sir", "Yours sincerely")
7. Bag of words, listed in alphabetical order

Other things we can do:
Metadata features:
1. If replied to sender earlier
2. If sender sent email but user ignored.
3. Sender email address. Internal email, external, from .com?
4. Many people in CC? Blasted email

*/
package com.expee.ml.utils;

import com.expee.ml.utils.Email;
import java.util.*;
import java.util.regex.*;
import java.io.*;
public class GetFeature {
  public static void makeEmailSetFeatures(Set<Email> emails) {
    int MIN_COUNT = 10;
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
    for (Map.Entry<String, Integer> entry: wordCount.entrySet()) {
      if (entry.getValue() >= MIN_COUNT) {
        wordMap.put(entry.getKey(), idx);
        idx++;
      }
    }
    for (Email email : emails) {
      GetFeature.printEmailFeatures(wordMap, email);
    }
  }
  public static void printEmailFeatures(Map<String, Integer> wordMap, Email email) {
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
    System.out.println("Has a reply: "+ (email.getChildren().size() != 0));
    String msg = email.getText();
    System.out.println("Message length (bytes): " + msg.length());
    String[] wordArray = msg.split("\\s");
    int numWords = wordArray.length;
    System.out.println("Message length (words): " + numWords);
    int numQuestionMarks = 0;
    for (int i = 0; i < msg.length(); i++) {
      if (msg.charAt(i) == '?') {
        numQuestionMarks++;
      }
    }
    System.out.println("Number of question marks: " + numQuestionMarks);
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
    System.out.println("Number of formal words: " + numFormalWords);
    System.out.println("Number of iterrogative words: " + numQuestionWords);
    System.out.println("Bag of words: Words, Word count");
    for (int i = 0; i < bagOfWords.length; i++) {
      System.out.print(bagOfWords[i] + " ");
    }
    System.out.println("");
  }
}