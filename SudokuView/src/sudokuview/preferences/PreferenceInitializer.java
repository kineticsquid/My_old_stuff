/*******************************************************************************
 * Copyright 2006 IBM by John Kellerman
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package sudokuview.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import sudokuview.preferences.PreferenceConstants;
import sudokuview.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.SUDOKU_MATRIX_SIZE, 9);
		store.setDefault(PreferenceConstants.SUDOKU_NEW_PUZZLE, PreferenceConstants.SUDOKU_NEW_PUZZLE_BLANK);
		store.setDefault(PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL, PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_EASY);
		store.setDefault(PreferenceConstants.SUDOKU_PUZZLE_FONT, 0);
		store.setDefault(PreferenceConstants.SUDOKU_MATRIX_COLOR, 0);
		store.setDefault(PreferenceConstants.SUDOKU_INPUT_COLOR, 0);
		store.setDefault(PreferenceConstants.SUDOKU_ERROR_COLOR, 0);
		store.setDefault(PreferenceConstants.SUDOKU_BACKGROUND_COLOR, 0);
	}

}
