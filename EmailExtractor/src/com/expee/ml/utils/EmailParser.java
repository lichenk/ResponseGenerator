package com.expee.ml.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmailParser {
  
  private static boolean isEmailDelim(String line) {
    return line.contains("----") && line.contains("Original Message");
  }

  private static boolean isInvalidLine(String line) {
    return line.contains("----") && line.contains("Forwarded");
  }
  
  public static ArrayList<Email> parseEmails(File file) throws IOException {
    ArrayList<Email> emails = new ArrayList<Email>();
    boolean first = true;
    
    List<String> lines = new ArrayList<String>();
    BufferedReader in = new BufferedReader(new FileReader(file));
    String nextline;
    while ((nextline = in.readLine()) != null) {
      if (isInvalidLine(nextline)) {
        return new ArrayList<Email>();
      }
      if (isEmailDelim(nextline)) {
        Email email = new Email(lines, first);
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
