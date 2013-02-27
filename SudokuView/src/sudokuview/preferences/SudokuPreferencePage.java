/*******************************************************************************
 * Copyright 2006 IBM by John Kellerman
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package sudokuview.preferences;

import sudokuview.Activator;
import sudokuview.preferences.PreferenceConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class SudokuPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public SudokuPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A demonstration of a preference page implementation");
	}

	Button matrixSizeThree, matrixSizeSix, matrizSizeNine, newPuzzleBlank, newPuzzleGenerated,
			difficultyLevelEasy, difficultyLevelMedium, difficultyLevelHard;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NULL);
		GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
		mainComposite.setLayoutData(data);

		new PreferenceLinkArea(parent, SWT.WRAP,
				"org.eclipse.ui.preferencePages.ColorsAndFonts",
				"See <a>''{0}''</a> for Sudoku prefs",
				(IWorkbenchPreferenceContainer) getContainer(), null);
		new Label(parent, SWT.NONE);
		
		Group numberGroup = new Group(parent, SWT.NONE);
		numberGroup
				.setText("Number of rows and columns. Takes effect when you start a new puzzle.");
		numberGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		numberGroup.setLayout(new GridLayout());
		matrixSizeThree = new Button(numberGroup, SWT.RADIO);
		matrixSizeThree.setText("3");
		matrixSizeSix = new Button(numberGroup, SWT.RADIO);
		matrixSizeSix.setText("6");
		matrizSizeNine = new Button(numberGroup, SWT.RADIO);
		matrizSizeNine.setText("9");
		Group newPuzzleGroup = new Group(parent, SWT.NONE);
		newPuzzleGroup.setText("Type of new puzzle.");
		newPuzzleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newPuzzleGroup.setLayout(new GridLayout());
		newPuzzleBlank = new Button(newPuzzleGroup, SWT.RADIO);
		newPuzzleBlank.setText("Blank - you enter the inputs");
		newPuzzleGenerated = new Button(newPuzzleGroup, SWT.RADIO);
		newPuzzleGenerated
				.setText("Generated - we provide the initial input values");
		Group difficultyGroup = new Group(parent, SWT.NONE);
		difficultyGroup.setText("Difficulty level of generated puzzle.");
		difficultyGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		difficultyGroup.setLayout(new GridLayout());
		difficultyLevelEasy = new Button(difficultyGroup, SWT.RADIO);
		difficultyLevelEasy.setText("Easy");
		difficultyLevelMedium = new Button(difficultyGroup, SWT.RADIO);
		difficultyLevelMedium.setText("Medium");
		difficultyLevelHard = new Button(difficultyGroup, SWT.RADIO);
		difficultyLevelHard.setText("Hard");
		setPreferenceValues();
		return mainComposite;
	}

	protected void setPreferenceValues() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		setMatrixSizePreference(prefs
				.getInt(PreferenceConstants.SUDOKU_MATRIX_SIZE));
		setNewPuzzlePreference(prefs
				.getInt(PreferenceConstants.SUDOKU_NEW_PUZZLE));
		setDifficultyLevelPreference(prefs
				.getInt(PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL));
	}

	protected void setDifficultyLevelPreference(int difficultyLevel) {
		if (difficultyLevel == PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_EASY) {
			difficultyLevelEasy.setSelection(true);
			difficultyLevelMedium.setSelection(false);
			difficultyLevelHard.setSelection(false);
		} else {
			if (difficultyLevel == PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_MEDIUM) {
				difficultyLevelEasy.setSelection(false);
				difficultyLevelMedium.setSelection(true);
				difficultyLevelHard.setSelection(false);
			} else {
				difficultyLevelEasy.setSelection(false);
				difficultyLevelMedium.setSelection(false);
				difficultyLevelHard.setSelection(true);
			}
		}
	}

	protected void setNewPuzzlePreference(int newPuzzle) {
		if (newPuzzle == PreferenceConstants.SUDOKU_NEW_PUZZLE_BLANK) {
			newPuzzleBlank.setSelection(true);
			newPuzzleGenerated.setSelection(false);
		} else {
			newPuzzleBlank.setSelection(false);
			newPuzzleGenerated.setSelection(true);
		}
	}

	protected void setMatrixSizePreference(int matrixSize) {
		if (matrixSize == 3) {
			matrixSizeThree.setSelection(true);
			matrixSizeSix.setSelection(false);
			matrizSizeNine.setSelection(false);
		} else {
			if (matrixSize == 6) {
				matrixSizeThree.setSelection(false);
				matrixSizeSix.setSelection(true);
				matrizSizeNine.setSelection(false);
			} else {
				matrixSizeThree.setSelection(false);
				matrixSizeSix.setSelection(false);
				matrizSizeNine.setSelection(true);
			}
		}
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		setMatrixSizePreference(prefs
				.getDefaultInt(PreferenceConstants.SUDOKU_MATRIX_SIZE));
		setNewPuzzlePreference(prefs
				.getDefaultInt(PreferenceConstants.SUDOKU_NEW_PUZZLE));
		setDifficultyLevelPreference(prefs
				.getDefaultInt(PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL));
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setValue(PreferenceConstants.SUDOKU_MATRIX_SIZE,
				getMatrixSizePreferenceValue(prefs));
		prefs.setValue(PreferenceConstants.SUDOKU_NEW_PUZZLE,
				getNewPuzzlePreferenceValue(prefs));
		prefs.setValue(PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL,
				getDifficultyLevelPreferenceValue(prefs));
		return super.performOk();
	}

	protected int getMatrixSizePreferenceValue(IPreferenceStore prefs) {
		if (matrixSizeThree.getSelection()) {
			return 3;
		} else {
			if (matrixSizeSix.getSelection()) {
				return 6;
			} else {
				return 9;
			}
		}
	}

	protected int getNewPuzzlePreferenceValue(IPreferenceStore prefs) {
		if (newPuzzleBlank.getSelection()) {
			return PreferenceConstants.SUDOKU_NEW_PUZZLE_BLANK;
		} else {
			return PreferenceConstants.SUDOKU_NEW_PUZZLE_GENERATED;
		}
	}

	protected int getDifficultyLevelPreferenceValue(IPreferenceStore prefs) {
		if (difficultyLevelEasy.getSelection()) {
			return PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_EASY;
		} else {
			if (difficultyLevelMedium.getSelection()) {
				return PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_MEDIUM;
			} else {
				return PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_HARD;
			}
		}
	}
}