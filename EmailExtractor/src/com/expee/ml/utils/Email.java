package com.expee.ml.utils;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class Email {
  private Email child;
  
  private String text;
  private String subject;
  
  private String sender;
  private Set<String> receivers;
  
  private Date date;
  
  public Email(List<String> lines, boolean first) {
    boolean inText = false;
    boolean hitSubject = false;
    
    StringBuffer textBuf = new StringBuffer();
    for (String line : lines) {
      if (line.contains("From:")) {
        this.sender = line.substring(line.indexOf("From:"));
      }
      
      if (line.contains("Subject:")) {
        this.subject = line.substring(line.indexOf("Subject:"));
        hitSubject = true;
      }
      if (hitSubject && !line.contains(":")) {
        inText = true;
      }
      
      if (inText) {
        textBuf.append(line);
      }
    }
    
    this.text = textBuf.toString();
  }

  public Email getChild() {
    return this.child;
  }
  
  public void setChild(Email child) {
    this.child = child;
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
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Email) {
      Email email = (Email) o;
      return nullEqual(child, email.child) && nullEqual(text, email.text) && 
          nullEqual(subject, email.subject) && nullEqual(sender, email.sender) && 
          nullEqual(sender, email.sender) && nullEqual(receivers, email.receivers) &&
          nullEqual(date, email.date);
    } else {
      return false;
    }
  }
}
