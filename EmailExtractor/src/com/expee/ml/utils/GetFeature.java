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
  private static final int MIN_COUNT = 1000;
  private static final Set<String> QUESTION_SET = new HashSet<String>(Arrays.asList(
      "Could", "Would", "Who", "When", "Where", "What", 
      "Why", "How", "Is", "Are", "Will", "May", "Might"));
  private static final Set<String> FORMAL_SET = new HashSet<String>(Arrays.asList(
      "Yours", "Sincerely", "Sir", "Regards", "Madam"));
  private static final Set<String> MEETING_SET = new HashSet<String>(Arrays.asList(
      "reminder", "meeting", "location", "date", "time"));
  private static final Set<String> REPLY_SET = new HashSet<String>(Arrays.asList(
      "reply", "rsvp", "respond", "response", "acknowledge", "email"));
  private static final String[] TRIGGER_PHRASE_ARRAY = {
      "follow up", "let me know", "let us know", "feel free", "help us", "get back"};
  
  public static void makeEmailSetFeatures(Set<Email> emails, String output) throws IOException {
    PrintWriter writer = new PrintWriter(new FileWriter(new File(output), true));

    Map<String, Integer> wordCount = new HashMap<String, Integer>();
    for (Email email: emails) {
      String subject = email.getSubject();
      if (subject == null) continue;
      String[] wordArray = subject.split("\\s");
      for (String word : wordArray) {
        String strippedLowerWord = word.replaceAll("[^\\w]","").toLowerCase();
        if (strippedLowerWord.length() > 0 && wordCount.containsKey(strippedLowerWord)) {
          wordCount.put(strippedLowerWord, wordCount.get(strippedLowerWord)+1);
        } else {
          wordCount.put(strippedLowerWord, 1);
        }
      }
    }
    
    System.out.println("Done making count map for subjects");

    writer.print("Byte Length,Word Length,Num Question,Num Question Words,Num Formal Words,");
    writer.print("Num Paragraphs,Paragraph Density,Num Recipients,Is Sender Enron,");
    writer.print("Num Meeting Words,Num Replyrelated words,Num Trigger Phrases,");

    Map<String, Integer> wordMap = new HashMap<String, Integer>();
    int idx = 0;
    for (Entry<String, Integer> entry : wordCount.entrySet()) {
      if (entry.getValue() >= MIN_COUNT) {
        writer.print(entry.getKey() + ",");
        wordMap.put(entry.getKey(), idx);
        idx++;
      }
    }
    writer.println("Num Replies, Word Length of Reply");
    int wordMapSize = wordMap.size();
    for (Email email : emails) {
      GetFeature.printEmailFeatures(wordMap, wordMapSize, writer, email);
    }
    writer.flush();
    writer.close();
  }
  
  public static void printEmailFeatures(
      Map<String, Integer> wordMap, int wordMapSize, PrintWriter writer, Email email)  throws IOException {

    String to = email.getTo();
    int numRecipients = 1;
    if (to != null) {
      numRecipients = to.split("\\w@\\w").length - 1;
    }

    int isEnron = 0;
    String sender = email.getSender();
    if (sender != null && sender.toLowerCase().contains("@enron")) {
      isEnron = 1;
    }
    String msg = email.getText();
    String[] paragraphArray = msg.split("\n\\w");
    int numParagraphs = paragraphArray.length;
    int paragraphDensity = 0;
    for (String paragraph: paragraphArray) {
      int numWordsInParagraph = paragraph.split("\\s").length;
      paragraphDensity += numWordsInParagraph;
    }
    if (numParagraphs > 0) {
      paragraphDensity /= numParagraphs;
    }

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
    int numMeetingWords = 0;
    int numReplyWords = 0;
    
    for (String word : wordArray) {
      String strippedWord = word.replaceAll("[^\\w]","");
      String strippedLowerWord = strippedWord.toLowerCase();
      
      if (strippedLowerWord.length() == 0) continue;
      // Preserve upper case to differentiate "Is...?" "Are you...?" from "...is..."
      if (QUESTION_SET.contains(strippedWord)) {
        numQuestionWords++;
      }
      // Preserve upper case to differentiate "Yours (truly)" from "(this is) yours"
      if (FORMAL_SET.contains(strippedWord)) {
        numFormalWords++;
      }
      if (MEETING_SET.contains(strippedLowerWord)) {
        numMeetingWords++;
      }
      if (REPLY_SET.contains(strippedLowerWord)) {
        numReplyWords++;
      }
    }
    String msglower = msg.toLowerCase();
    int numPhrases = 0;
    for (String phrase : TRIGGER_PHRASE_ARRAY) {
      if (msglower.contains(phrase)) {
        numPhrases++;
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

    int[] bagOfWords = new int[wordMapSize];
    for (int i = 0; i < wordMapSize; i++) {
      bagOfWords[i] = 0;
    }
    String subject = email.getSubject();
    if (subject != null) {
      String[] subjectWordArray = subject.split("\\s");
      for (String word : subjectWordArray) {
        String strippedLowerWord = word.replaceAll("[^\\w]","").toLowerCase();
        if (wordMap.containsKey(strippedLowerWord)) {
          bagOfWords[wordMap.get(strippedLowerWord)]++;
        }
      }
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
    // Number of paragraphs
    writer.print(numParagraphs + ",");
    // Paragraph density
    writer.print(paragraphDensity + ",");
    // Number of recipients
    writer.print(numRecipients + ",");
    // Is sender domain from enron.com?
    writer.print(isEnron + ",");
    // Meeting words (number)
    writer.print(numMeetingWords + ",");
    // Reply-related words
    writer.print(numReplyWords + ",");
    // Number of trigger phrases
    writer.print(numPhrases + ",");
    // Bag of words for subject
    for (int i = 0; i < bagOfWords.length; i++) {
      writer.print(bagOfWords[i] + ",");
    }
    // Number of replies this email has
    writer.print(numChildren + ",");
    // Average children size
    writer.println(averageChildrenSize);
  }
}