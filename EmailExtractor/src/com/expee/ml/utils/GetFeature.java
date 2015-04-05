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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GetFeature {
  private static final int MIN_COUNT = 10;

  private static final Set<String> QUESTION_SET = new HashSet<String>(Arrays.asList(
      "Could", "Would", "Who", "When", "Where", "What", 
      "Why", "How", "Is", "Are", "Will", "May", "Might"));
  private static final Set<String> FORMAL_SET = new HashSet<String>(Arrays.asList(
      "Yours", "Sincerely", "Sir", "Regards"));
  
  public static void makeEmailSetFeatures(Set<Email> emails, String output) throws IOException {
    PrintWriter writer = new PrintWriter(new FileWriter(new File(output), true));
    
    Map<String, Integer> wordCount = new HashMap<String, Integer>();
    for (Email email: emails) {
      String msg = email.getText();
      String[] wordArray = msg.split("\\s");
      for (String word: wordArray) {
        String strippedLowerWord = word.replaceAll("[^\\w]","").toLowerCase();
        if (strippedLowerWord.length() > 0) {
          if (wordCount.containsKey(strippedLowerWord)) {
            wordCount.put(strippedLowerWord, wordCount.get(strippedLowerWord)+1);
          } else {
            wordCount.put(strippedLowerWord, 1);
          }
        }
      }
    }
    
    System.out.println("Done making count map");
    
    Map<String, Integer> wordMap = new HashMap<String, Integer>();
    int idx = 0;
    writer.print("Byte Length,Word Length,Num Question,Num Question Words,Num Formal Words,");
    for (Entry<String, Integer> entry : wordCount.entrySet()) {
      if (entry.getValue() >= MIN_COUNT) {
        writer.print(entry.getKey() + ",");
        wordMap.put(entry.getKey(), idx);
        idx++;
      }
    }
    
    writer.println("Num Replies, Word Length of Reply");
    for (Email email : emails) {
      GetFeature.printEmailFeatures(wordMap, writer, email);
    }
    writer.flush();
    writer.close();
  }
  
  public static void printEmailFeatures(
      Map<String, Integer> wordMap, PrintWriter writer, Email email)  throws IOException {
    String msg = email.getText();
    String[] wordArray = msg.split("\\s");
    
    int numWords = wordArray.length;
    email.setWordCount(numWords);
    int numQuestionMarks = 0;
    for (int i = 0; i < msg.length(); i++) {
      if (msg.charAt(i) == '?') {
        numQuestionMarks++;
      }
    }
    
    int numQuestionWords = 0;
    int numFormalWords = 0;
    int[] bagOfWords = new int[wordMap.size()];
    
    for (String word : wordArray) {
      String strippedWord = word.replaceAll("[^\\w]","");
      String strippedLowerWord = strippedWord.toLowerCase();
      
      if (strippedLowerWord.length() == 0) continue;
      
      if (QUESTION_SET.contains(strippedWord)) {
        numQuestionWords++;
      }
      if (FORMAL_SET.contains(strippedWord)) {
        numFormalWords++;
      }
      if (wordMap.containsKey(strippedLowerWord)) {
        bagOfWords[wordMap.get(strippedLowerWord)]++;
      }
    }
    Set<Email> children = email.getChildren();
    int numChildren = children.size();
    int averageChildrenSize = 0;
    if (numChildren > 0) {
      for (Email child: children) {
        int numWordsInChild = child.getWordCount();
        if (numWordsInChild == -1) {
          numWordsInChild = child.getText().split("\\s").length;
          child.setWordCount(numWordsInChild);
        }
        averageChildrenSize += numWordsInChild;
      }
      averageChildrenSize /= numChildren;
    }

    //Message length (bytes)
    writer.print(msg.length() + ",");
    // Message length (words)
    writer.print(numWords + ",");
    // Number of question marks
    writer.print(numQuestionMarks + ",");
    // Number of formal words:
    writer.print(numFormalWords + ",");
    // Number of interrogative words
    writer.print(numQuestionWords + ",");
    // Bag of words
    for (int i = 0; i < bagOfWords.length; i++) {
      writer.print(bagOfWords[i] + ",");
    }
    // Number of replies this email has
    writer.print(numChildren + ",");
    writer.println(averageChildrenSize);
  }
}