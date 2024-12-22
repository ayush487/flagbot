package com.ayushtech.flagbot.atlas;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AtlasQuestion {
  private int qId;
  private String question;
  private Set<String> answerSet;

  public AtlasQuestion(int qId,String question, String answers) {
    this.qId = qId;
    this.question = question;
    this.answerSet = Arrays.stream(answers.split(",")).map(eachAnswer -> eachAnswer.toLowerCase().trim())
        .collect(Collectors.toSet());
  }

  public int getqId() {
    return qId;
  }

  public String getQuestion() {
    return question;
  }

  public Set<String> getAnswerSet() {
    return answerSet;
  }

  public boolean checkAnswer(String answer) {
    if (answerSet.contains(answer)) {
      answerSet.remove(answer);
      return true;
    }
    return false;
  }
  
}