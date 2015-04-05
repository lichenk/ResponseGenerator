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
  
  private Date date;
  
  public Email(List<String> lines, boolean first) {
    boolean inText = false;
    boolean hitSubject = false;
    this.children = new HashSet<Email>();
    
    StringBuffer textBuf = new StringBuffer();
    for (String line : lines) {
      if (line.contains("From:")) {
        this.sender = line.substring(line.indexOf("From:") + "From:".length()).trim();
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
    
    this.text = textBuf.toString().trim();
  }

  public Set<Email> getChildren() {
    return children;
  }
  
  public void addChild(Email child) {
    children.add(child);
  }

  public void merge(Email other) {
    children.addAll(other.getChildren());
  }

  public String getText() {
    return this.text;
  }

  private int nullCode(Object o) {
    return (o == null) ? 0 : o.hashCode();
  }
  
  @Override
  public int hashCode() {
    return nullCode(text) ^ nullCode(sender) ^ nullCode(subject);
  }
  
  private boolean nullEqual(Object a, Object b) {
    return a == b || a.equals(b);
  }

  public String toString() {
    return getText();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Email) {
      Email email = (Email) o;
      return nullEqual(text, email.text) && 
          nullEqual(subject, email.subject) && 
          nullEqual(sender, email.sender) && 
          nullEqual(date, email.date);
    } else {
      return false;
    }
  }
}
