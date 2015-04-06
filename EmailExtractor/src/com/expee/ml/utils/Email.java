// TODO: get date from email

package com.expee.ml.utils;

import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class Email {
  private Set<Email> children;
  
  private String text;
  private String subject;
  
  private String sender;

  private String to;
  
  private Date date;
  // wordCount is initialized to -1. It is set when computed in GetFeature to avoid recomputation.
  // I need this to get the wordCount of an email's children.
  // The reason why I'm doing this is because:
  // 1. The main cost in computing wordCount is performing string.split(), but I memoize string.split()
  // because it will take too much memory.
  // 2. Only replies need to have wordCount recomputed. This ensure the recomputation only occurs
  // at most once and only for emails which are replies, which is about 1/10th of the data set.
  private int wordCount;
  
  public Email(List<String> lines, boolean first) {
    boolean inText = false;
    boolean hitSubject = false;
    this.children = new HashSet<Email>();
    
    StringBuffer textBuf = new StringBuffer();
    for (String line : lines) {
      if (line.contains("From:") && line.contains("@")) {
        this.sender = line;
      }
      
      if (line.contains("To:") && line.contains("@")) {
        this.to = line;
      }

      if (line.contains("Subject:")) {
        this.subject = line.substring(line.indexOf("Subject:") + "Subject:".length()).trim();
        hitSubject = true;
      }
      if (hitSubject && !line.contains(":")) {
        inText = true;
      }
      
      if (inText) {
        textBuf.append(line.trim() + "\n");
      }
    }
    
    this.text = textBuf.toString().trim().replaceAll("=20", " ");
    this.wordCount = -1;
  }

  public Set<Email> getChildren() {
    return children;
  }
  
  public void addChild(Email child) {
    children.add(child);
  }

  public int getWordCount() {
    return this.wordCount;
  }

  public void setWordCount(int wordCount) {
    this.wordCount = wordCount;
  }

  public void merge(Email other) {
    children.addAll(other.getChildren());
  }

  public String getText() {
    return this.text;
  }

  public String getSubject() {
    return this.subject;
  }

  public String getSender() {
    return this.sender;
  }

  public String getTo() {
    return this.to;
  }

  private int nullCode(Object o) {
    return (o == null) ? 0 : o.hashCode();
  }
  
  @Override
  public int hashCode() {
    return nullCode(text) ^ nullCode(sender) ^ nullCode(subject);
  }
  
  private boolean nullEqual(Object a, Object b) {
    return (a != null && b != null && a.equals(b)) || (a == b);
  }

  public String toString() {
    return getText();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Email) {
      Email email = (Email) o;
      return nullEqual(subject, email.subject) && 
          nullEqual(sender, email.sender) && 
          nullEqual(date, email.date) &&
          nullEqual(text.length(), email.text.length()) &&
          nullEqual(text, email.text);
    } else {
      return false;
    }
  }
}
