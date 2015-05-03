package com.expee.ml;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.expee.ml.utils.Email;
import com.expee.ml.utils.EmailParser;
import com.expee.ml.utils.GetFeature;
import com.expee.ml.utils.OldGetFeature;

public class ExtractEmails {
  private static final String BASE_DIR = "/home/usert/enronsmall";
  private static final String OUTPUT = "EmailData.csv";
  private static final boolean INBOX_ONLY = false; // Set to true to restrict to "inbox" folders
  private static final boolean OLD_FEATURE = false; //Set to true for Bag of Words for email body

  private static void addDirectedEdge(Map<Email, Email> emails, Email parent, Email child) {
    emails.get(parent).addChild(emails.get(child));
  }

  private static void updateChildren(Map<Email, Email> emails, ArrayList<Email> emailChain) {
    for (int i = emailChain.size() - 1; i > 0; i--) {
      addDirectedEdge(emails, emailChain.get(i), emailChain.get(i - 1));
    }
  }
  
  public static Set<Email> extractStructure(List<File> files) throws IOException {
    Map<Email, Email> emails = new HashMap<Email, Email>();
    
    for (File emailFile : files) {
     if (!emailFile.isFile()) continue;
      
      ArrayList<Email> emailChain = EmailParser.parseEmails(emailFile);
      for (Email email : emailChain) {
        if (!emails.containsKey(email)) {
          emails.put(email, email);
        }
      }
      updateChildren(emails, emailChain);
    }

    return emails.keySet();
  }

  public static List<File> getEmailFiles(String dir) throws IOException {
    return getEmailFiles(dir, Integer.MAX_VALUE);
  }

  public static List<File> getLinearEmailFiles(String dir) {
    File base = new File(dir);
    List<File> emailFiles = new ArrayList<File>();

    for (File emailFile : base.listFiles()) {
      if (!emailFile.isFile() || emailFile.isHidden()) continue;
      emailFiles.add(emailFile);
    }

    return emailFiles;
  }

  public static void getEmailRecursive(File emailFile, List<File> emailFiles){
	  if (!emailFile.isDirectory()){
		  emailFiles.add(emailFile);
	  } else {
		  for (File subFile : emailFile.listFiles()){
			  getEmailRecursive(subFile, emailFiles);
		  }
	  }
  }

// TODO(Peijin): This code is buggy. It only goes down 3 levels, instead of recursing.
//Corrected for recursion
  public static List<File> getEmailFiles(String dir, int maxUsers) throws IOException {
    List<File> emailFiles = new ArrayList<File>();
    File base = new File(dir);
    int numUsers = 0;

    for (File user : base.listFiles()) {
      if (!user.isDirectory()) continue;
      
      for (File folder : user.listFiles()) {
        if (!folder.isDirectory()) continue;
        
        if (INBOX_ONLY && !folder.getName().equals("inbox")) continue;
        
        for (File emailFile : folder.listFiles()) {
          if (emailFile.isHidden()) continue;
          
          if (emailFile.isDirectory()){
        	  getEmailRecursive(emailFile, emailFiles);
          } else {
        	  emailFiles.add(emailFile);
          }
        }
        
      }

      System.out.println(user.getName() + " " + emailFiles.size());
      if (++numUsers >= maxUsers) break;
    }

    return emailFiles;
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Getting email list");
    // List<File> emailFiles = getLinearEmailFiles("../testsets/simple");
    List<File> emailFiles = getEmailFiles(BASE_DIR, 10);
    System.out.println("Extracting email structure");
    Set<Email> emails = extractStructure(emailFiles);
    System.out.println("Making features");
    if (OLD_FEATURE) {
      OldGetFeature.makeEmailSetFeatures(emails, OUTPUT);
    }
    else {
      GetFeature.makeEmailSetFeatures(emails, OUTPUT);
    }
  }
}
