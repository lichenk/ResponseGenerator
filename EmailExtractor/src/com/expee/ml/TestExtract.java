package com.expee.ml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.expee.ml.ExtractEmails;
import com.expee.ml.utils.Email;

public class TestExtract {
  public static void simpleTrace() throws IOException {
    String BASE_DIR = "../testsets/simple";
    List<File> emailFiles = ExtractEmails.getLinearEmailFiles(BASE_DIR);
    Set<Email> emails = ExtractEmails.extractStructure(emailFiles);
    for (Email email : emails) {
      String emailString = email.toString();
      if (emailString.contains("Wow Peijin")) {
        assert (email.getChildren().size() == 0);
      } else if (emailString.contains("OK, I’ve finished")) {
        assert (email.getChildren().size() == 3);
      } else if (emailString.contains("Alright, fine,")) {
        assert (email.getChildren().size() == 0);
      } else if (emailString.contains("The ML paper")) {
        assert (email.getChildren().size() == 2);
      } else if (emailString.contains("I’m playing dota.")) {
        assert (email.getChildren().size() == 1);
      } else if (emailString.contains("Peijin,")) {
        assert (email.getChildren().size() == 1);
      }
    }
  }

  public static void main (String[] args) throws IOException {
    simpleTrace();
  }
}