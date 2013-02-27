/*******************************************************************************
 * Copyright 2006 IBM by John Kellerman
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package sudoku;

import java.util.BitSet;
import java.util.Date;
import java.util.Random;

public class SudokuPuzzle {

	/**
	 * Specifies the number of elements in each row and column of
	 * the matrix
	 */
	protected int matrixSize;

	/**
	 * Specifies the number of elements in each row and column of
	 * each submatrix of the matrix. This is {@link #matrixSize }/3.
	 */
	protected int subMatrixSize;

	/**
	 * Matrix that contains the puzzle values. 0 indicates an element has no
	 * value. Matrix {@link #inputElements }indicates which are input values.
	 */
	protected int values[][];
	
	/**
	 * Arrays that define if a row, column, and submatrix contains a value. The size of the BitSets
	 * are matrixSize + 1. The submatrices are numbered starting in the upper left, moving across
	 * the columns, and then down to the next row.  For example, in a 9x9 puzzle, if element (0,4) 
	 * contains a "9". Then <code>rowContents[0].get(9) == true</code>, <code>columnContents[4].get(9) == true</code>, 
	 * and <code>submatrixContents[3].get(9) == true</code>. {@link #getSubMatrixFor(int, int)} returns the submatrix
	 * index for a given row and column.
	 */
	protected BitSet rowContents[], columnContents[], subMatrixContents[];
	
	/**
	 * Variables to indicate which rows, columns, and submatrices have errors. E.g. 
	 * <code>invalidRows.get(2) == true </code>means there is an error in the third row, index 2.
	 */
	protected BitSet invalidRows, invalidColumns, invalidSubMatrices;
	
	/**
	 * matrix of BitSets that indicate for each element in the matrix, what are the valid values
	 * that element can contain. E.g. <code>choicesRemainingForEmptyElement[1][2].toString == "{1, 4}"</code>
	 * means the valid values for element (1,2) can be 1 and 4.
	 */
	protected BitSet choicesRemainingForEmptyElement[][];
	
	/**
	 * These two variables contain the row and column of the empty matrix element that has the least number
	 * of remaining valid values. If more than one empty element have the same number of fewest choices,
	 * these variables refer to the first one encountered.
	 */
	protected int emptyElementWithFewestRemainingChoicesRow = 0;
	protected int emptyElementWithFewestRemainingChoicesColumn = 0;
	
	/**
	 * BitSet mask that is set to all 1's. It is used to invert the value of another BitSet.
	 */
	protected BitSet xorMask;

	/**
	 * Array that indicates which elements are input elements. That is which elements had defined values
	 * when ({@link #lockInputValues()} was invoked.
	 */
	protected BitSet inputElements[];

	/**
	 * Contains the solution to the puzzle.
	 */
	protected SudokuPuzzle solutionMatrix;

	/**
	 * Indicates a solution exists for the puzzle. The solution is defined in {@link #solutionMatrix}.
	 */
	protected boolean solutionKnown;

	/**
	 * Indicates the input values are locked and the puzzle is ready to be solved. It is set by 
	 * {@link #lockInputValues()} and reset by {@link #unlockInputValues()}.
	 */
	protected boolean inputsLocked;

	/**
	 * Used to generate random numbers to generate an matrix to be solved by the user.
	 */
	private Random numberGenerator;

	/**
	 * Constructs a new instance of this class given the size of the matrix. If
	 * the initial size is not a multiple of 3, it is adjusted to be so.
	 * 
	 * @param size
	 *            Number of elements in each row and column of the puzzle.
	 */

	public SudokuPuzzle(int size) {

		subMatrixSize = size / 3;
		matrixSize = subMatrixSize * 3;
		values = new int[matrixSize][matrixSize];
		invalidRows = new BitSet(matrixSize);
		invalidColumns = new BitSet(matrixSize);
		invalidSubMatrices = new BitSet(9);
		inputElements = new BitSet[matrixSize];
		rowContents = new BitSet[matrixSize];
		columnContents = new BitSet[matrixSize];
		subMatrixContents = new BitSet[9];	
		choicesRemainingForEmptyElement = new BitSet[matrixSize][matrixSize];
		for (int i = 0; i < matrixSize; i++) {
			inputElements[i] = new BitSet(matrixSize);
			rowContents[i] = new BitSet(matrixSize+1);
			columnContents[i] = new BitSet(matrixSize+1);
			for (int column = 0; column < matrixSize; column++) {
				values[i][column] = 0;
				BitSet b = new BitSet(matrixSize + 1);
				b.flip(0, matrixSize + 1);
				choicesRemainingForEmptyElement[i][column] = b;
			}
		}
		for (int i = 0; i < 9; i++) {
			subMatrixContents[i] = new BitSet(matrixSize+1);
		}
		inputsLocked = false;
		solutionKnown = false;
		xorMask = new BitSet(matrixSize + 1);
		xorMask.flip(0,matrixSize + 1);
	}

	/**
	 * Fills in the matrix with values to provide the user with a puzzle to solve. 
	 * 
	 * @param numberOfBlankElements number of elements to be left blank (at least) in matrix
	 */
	public void generateMatrixWithInputValues(int numberOfBlankElements) {
		numberGenerator = new Random(new Date().getTime());
		solutionMatrix = new SudokuPuzzle(matrixSize);
		//First generate a random, solved matrix
		generateARandomSolution();
		//Save this solution so it can be used through multiple trials. This is because the
		//algorithm to remove values from the values matrix will leave the solution matrix
		//with blank elements.
		SudokuPuzzle savedSolution = new SudokuPuzzle(matrixSize);
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				savedSolution.setValueAt(row, column, getSolutionValueAt(row, column));
			}
		}
		//Now, successively try to remove values from the values matrix while leaving 
		//a values matrix that has only one solution. 
		boolean done = false;
		while (!done) {
			copySolutionToValuesMatrix(savedSolution);
			removeValuesToCreateInputs(numberOfBlankElements);
			if (getNumberOfSolutions() <= 1 & getNumberOfBlankElements() >= numberOfBlankElements) {
				done = true;
			}
		}
		//Now we have a values matrix with at least numberOfBlankElements elements blank. 
		//Compute the solution and lock the input elements.
		computeSolutionFromInputs();
		lockInputValues();
	}

	/**
	 * Generate a random values matrix and solve it. Do this by filling in a random value in each row
	 * and solving the resulting matrix.
	 */
	private void generateARandomSolution() {
		int row, column;
		
		for (row = 0; row < matrixSize; row++) {
			column = nextNumber(0, matrixSize -1);
			setValueAt(row, column, nextNumber(1, matrixSize));
			if (isMatrixValid()) {
				if (!computeSolutionFromInputs()) {
					clearValueAt(row, column);
				}
			} else {
				clearValueAt(row, column);
			}
		}
		computeSolutionFromInputs();
	}
	
	/**
	 * Copies the solution matrix to the values matrix.
	 * 
	 * @param savedSolution - solution matrix to copy
	 */
	private void copySolutionToValuesMatrix(SudokuPuzzle savedSolution) {
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				setValueAt(row, column, savedSolution.getValueAt(row, column));
			}
		}
	}
	
	/**
	 * This removes values from the input matrix until there are at least <code>desiredNumberOfBlankElements</code> 
	 * in the input matrix and that there is one unique solution.
	 * 
	 * @param desiredNumberOfBlankElements - number of blank elements needed in the input matrix. Value depends on the difficult of the puzzle.
	 */
	private void removeValuesToCreateInputs(int desiredNumberOfBlankElements) {
		int row, column, oldValue1, oldValue2;
		//This BitSet keeps track of the choices we've already tried
		BitSet remainingChoices;
		remainingChoices = new BitSet(matrixSize * matrixSize / 2 + 1);
		remainingChoices.flip(0, matrixSize * matrixSize / 2 + 1);
		//nextChoiceOrdinal is the position in the sequence of remaining choices of the next choice
		int nextChoiceOrdinal;
		//Loop while there are choices left and we don't yet have enough blank elements
		//Clear two values because we want to keep the input matrix symmetrical
		//about the diagonal.
		while ((nextChoiceOrdinal = getNextChoiceToClear(remainingChoices)) >= 0 &
				getNumberOfBlankElements() < desiredNumberOfBlankElements) {
			row = nextChoiceOrdinal / matrixSize;
			column = nextChoiceOrdinal - (row * matrixSize);
			oldValue1 = getValueAt(row, column);
			oldValue2 = getValueAt((matrixSize - 1) - row, (matrixSize - 1) - column);
			clearValueAt(row, column);
			clearValueAt((matrixSize - 1) - row, (matrixSize - 1) - column);
			//If clearing these elements means there is more than one solution, 
			//put the element values back
			if (getNumberOfSolutions() > 1) {
				setValueAt(row, column, oldValue1);
				setValueAt((matrixSize - 1) - row, (matrixSize - 1) - column, oldValue2);
			}
		}
	}
	
	/**
	 * Given a BitSet of size m, with n bits set, where n <= m, this method 
	 * randomly returns the index of one of the n bits.
	 * @param possibleChoices - BitSet of possible choices. A choice is possible if its bit is set
	 * @return Ordinal representing position in sequence of next choice
	 */
	private int getNextChoiceToClear(BitSet possibleChoices) {
		if (possibleChoices.cardinality() == 0) {
			return -1;
		} else {
			int n = nextNumber(0, possibleChoices.cardinality()-1);
			int nextChoice = 0;
			if (n >= 0) {
				for (int i = 0; i < n; i++) {
					nextChoice = possibleChoices.nextSetBit(nextChoice+1);
				}
				nextChoice = possibleChoices.nextSetBit(nextChoice);
				possibleChoices.clear(nextChoice);
			} else {
				nextChoice = -1;
			}
			return nextChoice;
		}
	}

	/**
	 * Returns the number of possible solutions for a given input matrix.  
	 * @return Number of possible solutions for this input matrix, Returns 0, 1, or 2.
	 */
	private int getNumberOfSolutions() {
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				solutionMatrix.setValueAt(row, column, getValueAt(row, column));
			}
		}
		return solutionMatrix.computeNumberOfSolutions();
	}

	/**
	 * Returns a random integer between min and max inclusive
	 * @param min lower bound of range of random number
	 * @param max upper bound of range of random number
	 * @return random integer between min and max inclusive
	 */
	private int nextNumber(int min, int max) {
		return numberGenerator.nextInt(max - min + 1) + min;
	}
	
	/**
	 * Returns the number of blank elements in the input matrix
	 * @return number of blank elements in the input matrix
	 */
	private int getNumberOfBlankElements() {
		int numberOfBlankElements = 0;
		for (int row = 0; row < matrixSize; row++) {
			numberOfBlankElements += (matrixSize - rowContents[row].cardinality());
		}
		return numberOfBlankElements;
	}
	
	/**
	 * Returns the number of possible solutions for this input matrix.  Because we're only interested in
	 * 0, 1, or > 1 solutions, this method stops when it has found at least 2 solutions
	 * @return number of possible solutions for this input matrix.
	 */
	private int computeNumberOfSolutions() {
		if (isMatrixComplete() & isMatrixValid()) {
			return 1;
		} else {
			int currentRow = emptyElementWithFewestRemainingChoicesRow;
			int currentColumn = emptyElementWithFewestRemainingChoicesColumn;
			BitSet remainingChoices = choicesRemainingForEmptyElement[currentRow][currentColumn];
			int numberOfSolutions = 0;
			int nextValueToTry = 0;
			while ((nextValueToTry = remainingChoices.nextSetBit(nextValueToTry+1)) > 0 & numberOfSolutions < 2) {
				setValueAt(currentRow, currentColumn, nextValueToTry);
				numberOfSolutions += computeNumberOfSolutions();
				if (numberOfSolutions < 2) {
					clearValueAt(currentRow, currentColumn);
				}
			}
			return numberOfSolutions;
		}
	}

	/**
	 * Solves the puzzle starting with the first element. The solution is
	 * contained in {@link #solutionMatrix }.
	 * 
	 * @return true or false indicating whether or not the puzzle was solved.
	 */
	public boolean computeSolutionFromInputs() {
		solutionMatrix = new SudokuPuzzle(matrixSize);
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				solutionMatrix.setValueAt(row, column, getValueAt(row, column));
			}
		}
		solutionMatrix.computeSolution();
		if (solutionMatrix.isSolutionKnown()) {
			solutionKnown = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Solves the matrix recursively starting with the element (<code>row</code>,
	 * <code>column</code>. The solution attempts are contained in
	 * {@link #solutionMatrix }.
	 * 
	 * @param row
	 *            Row of element to start solution calculation with
	 * @param column
	 *            Column of element to start solution calculation with
	 * @return whether or not the matrix is solved at this point.
	 */
	private void computeSolution() {

		if (isMatrixComplete() & isMatrixValid()) {
			solutionKnown = true;
			return;
		} else {
			int currentRow = emptyElementWithFewestRemainingChoicesRow;
			int currentColumn = emptyElementWithFewestRemainingChoicesColumn;
			BitSet remainingChoices = choicesRemainingForEmptyElement[currentRow][currentColumn];
			int nextValueToTry = 0;
			while ((nextValueToTry = remainingChoices.nextSetBit(nextValueToTry+1)) > 0 & !solutionKnown) {
				setValueAt(currentRow, currentColumn, nextValueToTry);
				computeSolution();
				if (!solutionKnown) {
					clearValueAt(currentRow, currentColumn);
				}
			}
		}
	}

	/**
	 * Returns the value at element (row, column)
	 * 
	 * @param row
	 *            Row of element to return
	 * @param column
	 *            of element to return
	 * @return value in {@link #values } at (row, column)
	 */
	public int getValueAt(int row, int column) {
		return values[row][column];
	}

	/**
	 * Returns the value of the solution at element (row, column)
	 * 
	 * @param row
	 *            Row of element to return
	 * @param column
	 *            of element to return
	 * @return value in {@link #solutionMatrix } at (row, column)
	 */
	public int getSolutionValueAt(int row, int column) {
		return solutionMatrix.getValueAt(row, column);
	}

	/**
	 * Sets a value in {@link #values }. This results in a call to
	 * {@link #validateMatrix() } which causes the values in
	 * {@link #invalidElements } to be updated.
	 * 
	 * @param row
	 *            Row of element to set
	 * @param column
	 *            Column of element to set
	 * @param value
	 *            Value to set
	 */
	public void setValueAt(int row, int column, int value) {
		values[row][column] = value;
		validateMatrix(row, column);
	}

	/**
	 * Clears a value in {@link #values }. This results in a call to
	 * {@link #validateMatrix() } which causes the values in
	 * {@link #invalidElements } to be updated.
	 * 
	 * @param row
	 *            Row of element to clear
	 * @param column
	 *            Column of element to clear
	 */
	public void clearValueAt(int row, int column) {
		values[row][column] = 0;
		validateMatrix(row, column);
	}

	/**
	 * Whenever a matrix element changes, through {@link #setValueAt(int, int, int)} or {@link #clearValueAt(int, int)}, this
	 * method validates the matrix, marks invalid cells, and tracks which currently empty element can take the fewest values
	 * @param row row index of changed element
	 * @param column column index of changed element
	 */
	private void validateMatrix(int row, int column) {
		BitSet rowValues = new BitSet(matrixSize + 1);
		invalidRows.clear(row);
		//Check for invalid rows. Loop through each column in this row.
		for (int iColumn = 0; iColumn < matrixSize; iColumn++) {
			//If the element has a value and we've already seen this value in this row,
			//indicate an error in this row.
			if (values[row][iColumn]>0) {
				if (rowValues.get(values[row][iColumn])) {
					invalidRows.set(row);
				}
				//Indicate that we've seen this value in this row.
				rowValues.set(values[row][iColumn]);
			}
		}
		rowContents[row] = rowValues;
		
		BitSet columnValues = new BitSet(matrixSize + 1);
		invalidColumns.clear(column);
		//Check for invalid columns. Loop through each row in this column.
		for (int iRow = 0; iRow < matrixSize; iRow++) {
			//If the element has a value and we've already seen this value in this column,
			//indicate an error in this column.
			if (values[iRow][column]>0) {
				if (columnValues.get(values[iRow][column])) {
					invalidColumns.set(column);
				}
				//Indicate that we've seen this value in this column.
				columnValues.set(values[iRow][column]);
			}
		}
		columnContents[column] = columnValues;

		BitSet subMatrixValues = new BitSet(matrixSize + 1);
		invalidSubMatrices.clear(getSubMatrixFor(row, column));
		//Check for invalid submatrices. Loop through each element in the submatrix to which
		//element[row, column] belongs.
		for (int iRow = row / subMatrixSize * subMatrixSize; iRow < row / subMatrixSize * subMatrixSize + subMatrixSize; iRow++) {
			for (int iColumn = column / subMatrixSize * subMatrixSize; iColumn < column / subMatrixSize * subMatrixSize + subMatrixSize; iColumn++) {
				//If the element has a value and we've already seen this value in this submatrix,
				//indicate an error in this submatrix.
				if (values[iRow][iColumn]>0) {
					if (subMatrixValues.get(values[iRow][iColumn])) {
						invalidSubMatrices.set(getSubMatrixFor(row, column));
					}
					//Indicate that we've seen this value in this submatrix.
					subMatrixValues.set(values[iRow][iColumn]);
				}
			}
		}
		subMatrixContents[getSubMatrixFor(row, column)] = subMatrixValues;
		
		// Iterate through the whole matrix to determine which blank element has the fewest possible remaining choices for values.
		boolean newChoiceSet = false;

		for (int iRow = 0; iRow < matrixSize; iRow++) {
			for (int iColumn = 0; iColumn < matrixSize; iColumn++) {
				if (getValueAt(iRow, iColumn) == 0) {
					//This element is blank so construct a BitSit with each of the values in this element's row, column,
					//and submatrix. IOW, the values this element cannot take. Then invert the BitSet to leave the values 
					//it can take.
					BitSet b = new BitSet(matrixSize + 1);
					b.or(rowContents[iRow]);
					b.or(columnContents[iColumn]);
					b.or(subMatrixContents[getSubMatrixFor(iRow, iColumn)]);
					b.xor(xorMask);
					choicesRemainingForEmptyElement[iRow][iColumn] = b;
					//Now if this number of choices is a new minimum, set the fields which hold the row and column indices
					//of this element
					if (!newChoiceSet) {
						emptyElementWithFewestRemainingChoicesRow = iRow;
						emptyElementWithFewestRemainingChoicesColumn = iColumn;
						newChoiceSet = true;
					} else {
						if (b.cardinality() < choicesRemainingForEmptyElement[emptyElementWithFewestRemainingChoicesRow][emptyElementWithFewestRemainingChoicesColumn].cardinality() | 
								values[emptyElementWithFewestRemainingChoicesRow][emptyElementWithFewestRemainingChoicesColumn] != 0) {
							emptyElementWithFewestRemainingChoicesRow = iRow;
							emptyElementWithFewestRemainingChoicesColumn = iColumn;
							newChoiceSet = true;
						}
					}
				} else {
					//Else, element has a value, so it has no possible remaining choices
					choicesRemainingForEmptyElement[row][column] = new BitSet(matrixSize + 1);
				}
			}
		}
	}
	
	/**
	 * Given the row and column indices for an element, return it's submatrix index, an integer between 0 and 8
	 * @param row row index of element
	 * @param column column index of element
	 * @return submatrix index
	 */
	public int getSubMatrixFor(int row, int column) {
		return (row / subMatrixSize) * 3 + column / subMatrixSize;
	}

	/**
	 * Indicates of this row is valid. Returns true of the row has no invalid elements
	 * @param row index of row
	 * @return true if the row is valid
	 */
	public boolean isRowValid(int row) {
		return !invalidRows.get(row);
	}
	
	/**
	 * Indicates of this column is valid. Returns true of the column has no invalid elements
	 * @param column index of column
	 * @return true if the column is valid
	 */
	public boolean isColumnValid(int column) {
		return !invalidColumns.get(column);
	}
	
	/**
	 * Indicates of this submatrix is valid. Returns true of the submatrix has no invalid elements
	 * @param index index of submatrix
	 * @return true if the submatrix is valid
	 */
	public boolean isSubMatrixValid(int index) {
		return !invalidSubMatrices.get(index);
	}
	
	/**
	 * Indicates if the element at row, column is valid. The element is valid if no other element in the 
	 * same row, column, or submatrix has the same value.
	 * @param row row index of element
	 * @param column column index of element
	 * @return true if the element is valid
	 */
	public boolean isElementValid(int row, int column) {
		if (isRowValid(row) & isColumnValid(column)) {
			if (isSubMatrixValid(getSubMatrixFor(row, column))) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Return whether or not the specified element is marked as an input. This
	 * value is set by {@link #lockInputValues() } and reset by
	 * {@link #unlockInputValues() }.
	 * 
	 * @param row
	 *            Row index of element
	 * @param column
	 *            Column index of element
	 * @return true if the element is an input
	 */
	public boolean isElementInputAt(int row, int column) {
		return inputElements[row].get(column);
	}

	/**
	 * Marks the elements with defined values, that is the elements in
	 * {@link #values } that are non zero, as input elements.
	 */
	public void lockInputValues() {
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				if (values[row][column] != 0) {
					inputElements[row].set(column);
				}
			}
		}
		inputsLocked = true;
	}

	/**
	 * Marks all elements as not being input elements. This method also resets
	 * {@link #solutionKnown } to mark the puzzle as not solved.
	 */
	public void unlockInputValues() {
		for (int row = 0; row < matrixSize; row++) {
				inputElements[row].clear();
		}
		inputsLocked = false;
		solutionMatrix = null;
		solutionKnown = false;
	}

	/**
	 * Returns whether or not the input elements have been locked.
	 * 
	 * @return Value of {@link #inputsLocked }
	 */
	public boolean isInputsLocked() {
		return inputsLocked;
	}

	/**
	 * Returns whether or not the matrix is valud
	 * 
	 * @return Value of {@link #isMatrixValid() }
	 */
	public boolean isMatrixValid() {
		if (invalidRows.cardinality() > 0) {
			return false;
		} else {
			if (invalidColumns.cardinality() > 0) {
				return false;
			} else {
				if (invalidSubMatrices.cardinality() > 0) {
					return false;
				} else {
					return true;
				}
			}
		}
	}

	/**
	 * Returns whether or not a solution has been generated for the puzzle.
	 * 
	 * @return Value of {@link #solutionKnown }
	 */
	public boolean isSolutionKnown() {
		return solutionKnown;
	}

	public boolean isMatrixComplete() {
		boolean complete = true;
		int row = 0;
		while (complete & row < matrixSize) {
			if (rowContents[row].cardinality() < matrixSize) {
				complete = false;
			}
			row++;
		}
		return complete;
	}

	/**
	 * Returns the size of the matrix.
	 * 
	 * @return Value of {@link #matrixSize }
	 */
	public int getMatrixSize() {
		return matrixSize;
	}

	/**
	 * Returns the size of a submatrix of the matrix.
	 * 
	 * @return Value of {@link #subMatrixSize }
	 */
	public int getSubMatrixSize() {
		return subMatrixSize;
	}

	/**
	 * Sets solution matrix.
	 * 
	 * @param solutionMatrix
	 *            Instance of <code>Sudoku</code> the represents a solution to
	 *            the puzzle.
	 */
	public void setSolutionMatrix(SudokuPuzzle solutionMatrix) {
		this.solutionMatrix = solutionMatrix;
	}
	
	public void printSolutionMatrix() {
		solutionMatrix.printMatrix();
	}
	
	public void printMatrix() {
		System.out.println();
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				System.out.print(getValueAt(row, column) + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	public int[][] getValues() {
		return values;
	}

	public SudokuPuzzle getSolutionMatrix() {
		return solutionMatrix;
	}
}
