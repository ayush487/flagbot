package com.ayushtech.flagbot.crossword;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Level {

	private final int level;
	private int rows;
	private int columns;
	private Set<String> words;
	private final List<Character> allowed_letters;
	private char[][] grid_solved;
	private char[][] grid_unsolved;
	private String[] across_string;
	private String[] down_string;
	private int min_word_size;
	private final int max_word_size;

	public Level(int level, String main_word, String words_combined, String level_data) {
		this.level = level;
		this.max_word_size = main_word.length();
		allowed_letters = main_word.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
		Collections.shuffle(allowed_letters);
		this.words = new HashSet<>();
		String[] words_array = words_combined.split(",");
		this.min_word_size = words_array[0].length();
		for (String w : words_array) {
			words.add(w);
			if (w.length() < this.min_word_size) {
				this.min_word_size = w.length();
			}
		}
		extractLevels(level_data);
		setupExtra();
	}

	public void updateUnsolvedGrid(CorrectWordResponse response) {
		int wordLength = response.word().length();
		if (response.isAcross()) {
			int x = response.x();
			int y = response.y();
			for (int j = x; j < x + wordLength; j++) {
				grid_unsolved[y][j] = response.word().charAt(j - x);
			}
		} else {
			int x = response.x();
			int y = response.y();
			for (int i = y; i < y + wordLength; i++) {
				grid_unsolved[i][x] = response.word().charAt(i - y);
			}
		}
	}

	private void extractLevels(String levelData) {
		this.across_string = levelData.split(":");
		this.rows = across_string[0].length();
		this.columns = across_string.length;
		this.grid_solved = new char[columns][rows];
		this.grid_unsolved = new char[columns][rows];
		for (int i = 0; i < columns; i++) {
			for (int j = 0; j < rows; j++) {
				this.grid_solved[i][j] = across_string[i].charAt(j);
				if (across_string[i].charAt(j) == '-') {
					this.grid_unsolved[i][j] = '-';
				} else {
					this.grid_unsolved[i][j] = '+';
				}
			}
		}
	}

	private void setupExtra() {
		this.down_string = new String[rows];
		for (int j = 0; j < rows; j++) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < columns; i++) {
				sb.append(grid_solved[i][j]);
			}
			down_string[j] = sb.toString();
		}
	}

	public CorrectWordResponse checkWord(String word) {
		if (words.contains(word)) {
			words.remove(word);
			boolean isLevelCompleted = words.isEmpty();
			Pattern pattern = Pattern.compile(String.format("\\b%s\\b", word));
			boolean isAcross = true;
			for (int i = 0; i < columns; i++) {
				var matcher = pattern.matcher(across_string[i]);
				if (matcher.find()) {
					int x = matcher.start();
					int y = i;
					return new CorrectWordResponse(true, word, isAcross, x, y, isLevelCompleted);
				}
			}
			isAcross = false;
			for (int i = 0; i < rows; i++) {
				var matcher = pattern.matcher(down_string[i]);
				if (matcher.find()) {
					int x = i;
					int y = matcher.start();
					return new CorrectWordResponse(true, word, isAcross, x, y, isLevelCompleted);
				}
			}
			return new CorrectWordResponse(false, word, true, 0, 0, false);
		} else {
			return new CorrectWordResponse(false, "", false, 0, 0, false);
		}
	}

	public boolean checkExtraWordCompletion() {
		if (words.isEmpty()) {
			return false;
		}
		String[] across_temp = new String[columns];
		String[] down_temp = new String[rows];
		for (int i = 0; i < columns; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < rows; j++) {
				sb.append(Character.toLowerCase(grid_unsolved[i][j]));
			}
			across_temp[i] = sb.toString();
		}
		for (int j = 0; j < rows; j++) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < columns; i++) {
				sb.append(Character.toLowerCase(grid_unsolved[i][j]));
			}
			down_temp[j] = sb.toString();
		}
		for (String w : words) {
			Pattern p = Pattern.compile(String.format("(?<![a-zA-Z+])%s(?![a-zA-Z+])", w));
			boolean isFound = false;
			for (String a : across_temp) {
				var matcher = p.matcher(a);
				if (matcher.find()) {
					isFound = true;
					words.remove(w);
					if (words.isEmpty()) {
						return true;
					}
					break;
				}
			}
			if (!isFound) {
				for (String a : down_temp) {
					var matcher = p.matcher(a);
					if (matcher.find()) {
						words.remove(w);
						if (words.isEmpty()) {
							return true;
						}
						break;
					}
				}
			}
		}
		return false;
	}

	public void shuffleAllowedLetters() {
		Collections.shuffle(allowed_letters);
	}

	public char[][] getGridSolved() {
		return grid_solved;
	}

	public char[][] getGridUnsolved() {
		return grid_unsolved;
	}

	public int getLevel() {
		return this.level;
	}

	public int getColumns() {
		return this.columns;
	}

	public int getRows() {
		return this.rows;
	}

	public void unlockLetter(int c, int r) {
		grid_unsolved[c][r] = Character.toUpperCase(grid_solved[c][r]);
	}

	public String getAllowedLetters() {
		StringBuilder sb = new StringBuilder("**");
		this.allowed_letters.stream().map(ch -> Character.toUpperCase(ch)).forEach(c -> sb.append(c + " "));
		sb.append("**");
		return sb.toString();
	}

	public List<Character> getAllowedLetterList() {
		return this.allowed_letters;
	}

	public int getMinWordSize() {
		return this.min_word_size;
	}

	public int getMaxWordSize() {
		return this.max_word_size;
	}

	public void removeEnterredWords(String[] enterredWords) {
		for (String w : enterredWords) {
			words.remove(w);
		}
	}

	public void setUnsolvedGrid(String unsolvedGridData) {
		String tempAcrossString[] = unsolvedGridData.split(":");
		for (int i = 0; i < columns; i++) {
			for (int j = 0; j < rows; j++) {
				grid_unsolved[i][j] = tempAcrossString[i].charAt(j);
			}
		}
	}
}