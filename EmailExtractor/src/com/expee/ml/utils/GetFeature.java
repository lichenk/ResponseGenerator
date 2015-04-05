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
    public static void printEmailFeatures(Email email) {
        String[] questionWordArray = {"Could", "Would", "Who", "When", "Where", "What", "Why", "How", "Is", "Are", "Will", "May", "Might"};
        String[] formalWordArray = {"Yours", "Sincerely", "Sir", "Regards"};
        HashSet<String> questionWordSet = new HashSet<>();
        for (int i = 0; i < questionWordArray.length; i++) {
            questionWordSet.add(questionWordArray[i]);
        }
        HashSet<String> formalWordSet = new HashSet<>();
        for (int i = 0; i < formalWordArray.length; i++) {
            formalWordSet.add(formalWordArray[i]);
        }
        System.out.println("Has a reply: "+ (email.getChild() != null));
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
        HashMap<String, Integer> bagOfWords = new HashMap<>();
        for (int i = 0; i < numWords; i++) {
            String word = wordArray[i];
            String strippedWord = word.replaceAll("[^\\w]","");
            String strippedLowerWord = strippedWord.toLowerCase();
            if (questionWordSet.contains(strippedWord)) {
                numQuestionWords++;
            }
            if (formalWordSet.contains(strippedWord)) {
                numFormalWords++;
            }
            if (bagOfWords.containsKey(strippedLowerWord)) {
                bagOfWords.put(strippedLowerWord, bagOfWords.get(strippedLowerWord)+1);
            }
            else {
                bagOfWords.put(strippedLowerWord, 1);
            }
        }
        System.out.println("Number of formal words: " + numFormalWords);
        System.out.println("Number of iterrogative words: " + numQuestionWords);
        System.out.println("Bag of words: Words, Word count");
        Iterator it = bagOfWords.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry wordCountPair = (Map.Entry)it.next();
            System.out.println(wordCountPair.getKey() + " " + wordCountPair.getValue());
            it.remove();
        }
        System.out.println("");
    }
}