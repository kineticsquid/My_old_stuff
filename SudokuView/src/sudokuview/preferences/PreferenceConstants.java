/*******************************************************************************
 * Copyright 2006 IBM by John Kellerman
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package sudokuview.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String SUDOKU_MATRIX_SIZE = "Sudoku.matrixSizePreference";

	public static final String SUDOKU_NEW_PUZZLE = "Sudoku.newPuzzlePreference";
	public static final int SUDOKU_NEW_PUZZLE_BLANK = 0;
	public static final int SUDOKU_NEW_PUZZLE_GENERATED = 1;

	public static final String SUDOKU_DIFFICULTY_LEVEL = "Sudoku.difficultyLevelPreference";
	public static final int SUDOKU_DIFFICULTY_LEVEL_EASY = 0;
	public static final int SUDOKU_DIFFICULTY_LEVEL_MEDIUM = 1;
	public static final int SUDOKU_DIFFICULTY_LEVEL_HARD = 2;
	
	public static final String SUDOKU_PUZZLE_FONT = "Sudoku.puzzleFontPreference";

	public static final String SUDOKU_BACKGROUND_COLOR = "Sudoku.backgroundColorPreference";
	
	public static final String SUDOKU_MATRIX_COLOR = "Sudoku.matrixColorPreference";
	
	public static final String SUDOKU_ERROR_COLOR = "Sudoku.errorColorPreference";
	
	public static final String SUDOKU_INPUT_COLOR = "Sudoku.inputColorPreference";
	
}
