package com.expee.ml.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmailParser {
  
  private static boolean isEmailDelim(String line) {
    return line.contains("----") && line.contains("Original Message");
  }
  
  public static List<Email> parseEmails(File file) throws IOException {
    List<Email> emails = new ArrayList<Email>();
    boolean first = true;
    
    List<String> lines = new ArrayList<String>();
    BufferedReader in = new BufferedReader(new FileReader(file));
    String nextline;
    while ((nextline = in.readLine()) != null) {
      if (isEmailDelim(nextline)) {
        Email email = new Email(lines, first);
        if (emails.size() > 0) {
          email.setParent(emails.get(emails.size() - 1));
        }
        emails.add(email);
        lines.clear();
        first = false;
      } else {
        lines.add(nextline);
      }
    }
    if (lines.size() > 0) {
      emails.add(new Email(lines, first));
    }
    in.close();
    
    return emails;
  }
}
