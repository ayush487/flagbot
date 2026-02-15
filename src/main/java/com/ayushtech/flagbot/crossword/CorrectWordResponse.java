package com.ayushtech.flagbot.crossword;

public record CorrectWordResponse(boolean isCorrect,String word, boolean isAcross, int x, int y, boolean levelCompleted) {
}

