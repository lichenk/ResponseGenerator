package com.expee.ml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.expee.ml.utils.Email;
import com.expee.ml.utils.EmailParser;
import com.expee.ml.utils.GetFeature;

public class ExtractEmails {
  private static final String BASE_DIR = "/Users/Ananya/Downloads/enron/maildir";
  private static final String OUTPUT = "EmailData.csv";

  public static void addDirectedEdge(Map<Email, Email> emails, Email parent, Email child) {
    emails.get(parent).addChild(emails.get(child));
  }

  public static void updateChildren(Map<Email, Email> emails, ArrayList<Email> emailChain) {
    for (int i = emailChain.size() - 1; i > 0; i--) {
      addDirectedEdge(emails, emailChain.get(i), emailChain.get(i - 1));
    }
  }

  public static void extract(String dir, String output) throws IOException {
    extract(dir, output, Integer.MAX_VALUE);
  }
  
  public static void extract(String dir, String output, int maxUsers) throws IOException {
    File base = new File(dir);

    // This might seem like a hack, but basically an email's contents is used as a hashcode
    // for a specific instance of an email. We don't want references to different instances
    // representing the same email to be floating around.
    Map<Email, Email> emails = new HashMap<Email, Email>();
    int usersSeen = 0;

    for (File user : base.listFiles()) {
      if (!user.isDirectory()) continue;
      
      for (File folder : user.listFiles()) {
        if (!folder.isDirectory()) continue;
        
        for (File emailFile : folder.listFiles()) {
          if (!emailFile.isFile()) continue;

          ArrayList<Email> emailChain = EmailParser.parseEmails(emailFile);
          for (Email email : emailChain) {
            if (!emails.containsKey(email)) {
              emails.put(email, email);
            }
          }
          updateChildren(emails, emailChain);
        }
      }
      System.out.println(user.getName() + " " + emails.size());
      if (++usersSeen >= maxUsers) break;
    }
    System.out.println("Done?");
    GetFeature.makeEmailSetFeatures(emails.keySet(), OUTPUT);
  }
  
  public static void main(String[] args) throws Exception {
    ExtractEmails.extract(BASE_DIR, OUTPUT, 10);
  }
}
