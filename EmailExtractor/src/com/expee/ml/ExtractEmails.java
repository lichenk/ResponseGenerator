package com.expee.ml;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.expee.ml.utils.Email;
import com.expee.ml.utils.EmailParser;
import com.expee.ml.utils.GetFeature;

public class ExtractEmails {
  private static final String BASE_DIR = "/home/usert/enronsmall";
  private static final String OUTPUT = "EmailData.csv";
  
  public static void extract(String dir, String output) throws IOException {
    File base = new File(dir);

    Set<Email> emails = new HashSet<Email>();
    for (File user : base.listFiles()) {
      if (!user.isDirectory()) continue;
      
      for (File folder : user.listFiles()) {
        if (!folder.isDirectory()) continue;
        
        for (File email : folder.listFiles()) {
          if (!email.isFile()) continue;
          
          emails.addAll(EmailParser.parseEmails(email));
        }
      }
      System.out.println(user.getName() + " " + emails.size());
    }
    GetFeature.makeEmailSetFeatures(emails);
  }
  
  public static void main(String[] args) throws Exception {
    ExtractEmails.extract(BASE_DIR, OUTPUT);
    //for (Email x : EmailParser.parseEmails(new File("/Users/Ananya/Desktop/127"))) {
    //  System.out.println(x);
    //}
  }
}
