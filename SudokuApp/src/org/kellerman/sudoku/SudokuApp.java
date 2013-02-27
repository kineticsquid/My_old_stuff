package org.kellerman.sudoku;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class SudokuApp {

	/**
	 * Size of the puzzle. I.e. the number of rows and columns.
	 */
	static final int matrixSize = 9;
	
	/**
	 * Size of a submatrix. This is {@link #matrixSize } / 3.
	 */
	static int subMatrixSize;
	
	/**
	 * Size of an element of the matrix when it is drawn.
	 */
	static final int elementSize = 50;

	/**
	 * Distance between the drawn matrix and the edges of the client area
	 */
	static final int margin = 30;

	/**
	 * Offset, in x and y dimensions, to draw shadows for the matrix 
	 */
	static final int shadowOffset = 10;

	/**
	 * Offset, in x and y dimensions, to draw the currently selected element
	 */
	static final int selectionOffset = 6;

	/**
	 * Width of major lines of the matrix. The major lines are the outside edges and the
	 * the edges between the submatrices.
	 */
	static final int majorLineWidth = 2;

	/**
	 * Width of the minor lines of the matrix. The minor lines are the lines within the
	 * submatrices
	 */
	static final int minorLineWidth = 1;

	/**
	 * Constants to hold color values.
	 */
	static Color backgroundColor, matrixColor, shadowColor, errorColor, inputColor;

	/**
	 * Font used to draw the element values.
	 */
	static Font font;

	/**
	 * Application shell
	 */
	static Shell shell;

	/**
	 * Canvas for drawing the puzzle
	 */
	static Canvas canvas;

	/**
	 * Represents the currently selected element. 
	 */
	static Point currentSelection;

	/**
	 * Puzzle being solved.  
	 */
	static SudokuPuzzle puzzle;
	
	/**
	 * Variables to hold the menu items. We keep these in order to enable and disable them based
	 * on the state of the puzzle.
	 */
	static MenuItem lockInputsMenuItem, solveMenuItem, hintMenuItem, lockInputsPopUp, solvePopUp,
		hintPopUp;


	public static void main(String[] args) {
		puzzle = new SudokuPuzzle(matrixSize);
		subMatrixSize = puzzle.getSubMatrixSize();
		currentSelection = null;

		Display display = new Display();
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Sudoku");
		shell.setSize(elementSize * matrixSize + margin * 4, elementSize
				* matrixSize + margin * 4);
		//Add a listener to intercept when a user closes the application via the 'X' 
		//button in the upper right.
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event e) {
				exit();
			}
		});
		canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		canvas.setSize(elementSize * matrixSize + margin * 2, elementSize
				* matrixSize + margin * 2);
		canvas.setLocation(margin, margin);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				draw(e.gc);
			}
		});
		backgroundColor = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		shadowColor = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		matrixColor = display.getSystemColor(SWT.COLOR_WHITE);
		errorColor = display.getSystemColor(SWT.COLOR_RED);
		GC gc = new GC(canvas);
		RGB rgb = new RGB(236, 233, 216);
		inputColor = new Color(gc.getDevice(), rgb);
		font = new Font(display, "Tahoma", elementSize / 2, SWT.NORMAL);
		createMenus();
		createListeners();
		// Keep this line here. Moving it before the canvas creation
		// causes the drawing not to appear
		shell.open();
		draw(gc);
		gc.dispose();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		font.dispose();
		display.dispose();
	}

	public static void createMenus() {
		//Create menu bar and menu items
		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);
		MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
		fileItem.setText("&File");
		Menu submenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(submenu);
		MenuItem item = new MenuItem(submenu, SWT.PUSH);
		item.setText("&New\tCtrl+N");
		item.setAccelerator(SWT.CTRL + 'N');
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				newPuzzle();
			}
		});
		item = new MenuItem(submenu, SWT.PUSH);
		item.setText("&Exit\tCtrl+X");
		item.setAccelerator(SWT.CTRL + 'X');
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				exit();
			}
		});
		MenuItem puzzleItem = new MenuItem(bar, SWT.CASCADE);
		puzzleItem.setText("&Puzzle");
		submenu = new Menu(shell, SWT.DROP_DOWN);
		puzzleItem.setMenu(submenu);
		lockInputsMenuItem = new MenuItem(submenu, SWT.PUSH);
		lockInputsMenuItem.setText("&Lock Inputs\tCtrl+L");
		lockInputsMenuItem.setAccelerator(SWT.CTRL + 'L');
		lockInputsMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				lockMatrixInput();
			}
		});
		hintMenuItem = new MenuItem(submenu, SWT.PUSH);
		hintMenuItem.setText("&Hint\tCtrl+H");
		hintMenuItem.setAccelerator(SWT.CTRL + 'H');
		hintMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				provideHint();
			}
		});
		solveMenuItem = new MenuItem(submenu, SWT.PUSH);
		solveMenuItem.setText("&Solve Puzzle\tCtrl+S");
		solveMenuItem.setAccelerator(SWT.CTRL + 'S');
		solveMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				solvePuzzle();
			}
		});
		MenuItem helpItem = new MenuItem(bar, SWT.CASCADE);
		helpItem.setText("&Help");
		submenu = new Menu(shell, SWT.DROP_DOWN);
		helpItem.setMenu(submenu);
		item = new MenuItem(submenu, SWT.PUSH);
		item.setText("&Help");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				help();
			}
		});
		item = new MenuItem(submenu, SWT.PUSH);
		item.setText("&About Sudoku");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				about();
			}
		});
		//Now create popup menu and entries
		Menu menu = new Menu(shell, SWT.POP_UP);
		lockInputsPopUp = new MenuItem(menu, SWT.PUSH);
		lockInputsPopUp.setText("&Lock Inputs\tCtrl+L");
		lockInputsPopUp.setAccelerator(SWT.CTRL + 'L');
		lockInputsPopUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				lockMatrixInput();
			}
		});
		hintPopUp = new MenuItem(menu, SWT.PUSH);
		hintPopUp.setText("&Hint\tCtrl+H");
		hintPopUp.setAccelerator(SWT.CTRL + 'H');
		hintPopUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				provideHint();
			}
		});		
		solvePopUp = new MenuItem(menu, SWT.PUSH);
		solvePopUp.setText("&Solve Puzzle\tCtrl+S");
		solvePopUp.setAccelerator(SWT.CTRL + 'S');
		solvePopUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				solvePuzzle();
			}
		});
		canvas.setMenu(menu);
		//Set the enabled status of the menuItems based on the initial state of the puzzle.
		setMenuItemEnablement();
	}
	
	/**
	 * This method is called whenever there is a state change in the puzzle that can
	 * affect which menuItems should be enabled.
	 */
	public static void setMenuItemEnablement() {
		//First, look to see if a value has been entered in the matrix.
		boolean inputValueFound = false;
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				if (puzzle.getValueAt(row, column) > 0) {
					inputValueFound = true;
					break;
				}
			}
			if (inputValueFound) {
				break;
			}
		}
		//If a value has been entered, allow the entered values to be locked as inputs. Otherwise not.
		if (inputValueFound) {
			lockInputsMenuItem.setEnabled(true);
			lockInputsPopUp.setEnabled(true);
		} else {
			lockInputsMenuItem.setEnabled(false);
			lockInputsPopUp.setEnabled(false);
		}
		//If an element is selected and we have a solution
		if (currentSelection != null & puzzle.isPuzzleSolved()) {
			// If the current select is not an input element, allow a hint to be generated. Since the
			//navigation scheme prevents selecting an input element, this check is here to handle the
			//case in which the user locks the inputs (and the current selection is an input element
			//and so remains selected) and immediately asks for a hint.
			if (!puzzle.isElementInputAt(currentSelection.x, currentSelection.y)) {
				hintMenuItem.setEnabled(true);
				hintPopUp.setEnabled(true);
			//Else prevent a hint from being generated	
			} else {
				hintMenuItem.setEnabled(false);
				hintPopUp.setEnabled(false);
			}
		//Else an element is not selected, so prevent a hint from being generated.
		} else {
			hintMenuItem.setEnabled(false);
			hintPopUp.setEnabled(false);
		}
		//If a solution exists for the puzzle, allow the user to ask for the solution. Otherwise not.
		if (puzzle.isPuzzleSolved()) {
			solveMenuItem.setEnabled(true);
			solvePopUp.setEnabled(true);
		} else {
			solveMenuItem.setEnabled(false);
			solvePopUp.setEnabled(false);
		}
	}
	
	/**
	 * Helper method to display a message to the user.
	 * 
	 * @param msg Message to display.
	 */
	public static void displayMessage(String msg) {
		MessageBox m = new MessageBox(shell);
		m.setMessage(msg);
		m.open();
	}
	
	/**
	 * Ask the user a question. Return true if the user clicks on 'OK', false otherwise.
	 * 
	 * @param msg Question to ask
	 * @return True if the user clicks on 'OK', false otherwise.
	 */
	public static boolean askQuestion(String msg) {
		MessageBox m = new MessageBox(shell, SWT.OK | SWT.CANCEL);
		m.setMessage(msg);
		if (m.open() == SWT.OK) {
			return true;
		} else
			return false;
	}

	/**
	 * Handle user action to create a new puzzle. 
	 */
	public static void newPuzzle() {
		if (askQuestion("Are you sure you want to quit this puzzle and start a new one?")) {
			puzzle = new SudokuPuzzle(matrixSize);
			//Reset these menuItems
			lockInputsMenuItem.setText("&Lock Inputs\tCtrl+L");
			lockInputsPopUp.setText("&Lock Inputs\tCtrl+L");
			setMenuItemEnablement();
			redraw();
		}
	}

	/**
	 * Handle user action to exit the application.
	 */
	public static void exit() {
		if (askQuestion("Are you sure you want to quit?")) {
			shell.dispose();
		}
	}

	/**
	 * Handle user action to lock elements, with values defined, as inputs. Or if they have been
	 * locked, to unlock them.
	 */
	public static void lockMatrixInput() {
		//If inputs have not been locked.
		if (!puzzle.isInputsLocked()) {
			//Attempt to solve the puzzle.
			boolean solved = puzzle.solve();
			//If the puzzle can be solved, lock the input values and change the menu items 
			//to prompt to unlock the input values.
			if (solved) {
				puzzle.lockInputValues();
				lockInputsMenuItem.setText("Un&Lock Inputs\tCtrl+L");
				lockInputsPopUp.setText("Un&Lock Inputs\tCtrl+L");
			//Otherwise matrix can't be solved with currently defined values.	
			} else {
				displayMessage("Puzzle not solvable. Check your input values.");
			}
		//Else inputs are already locked, so unlock them and set the menu items.	
		} else {
			puzzle.unlockInputValues();
			lockInputsMenuItem.setText("&Lock Inputs\tCtrl+L");
			lockInputsPopUp.setText("&Lock Inputs\tCtrl+L");
		}
		setMenuItemEnablement();
		redraw();
	}

	/**
	 * Handle user action to solve the puzzle. Prompt the user and then copy 
	 * the solution values to the matrix.
	 */
	public static void solvePuzzle() {
		//First check to see if the user has entered values in the matrix since locking the 
		//inputs. If so, these values will be ovewritten. This is because the solution was generated
		//when the inputs were locked. Computing the solution everytime the user enters or changes
		//an input is not feasible.
		boolean nonInputValueFound = false;
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				if (puzzle.getValueAt(row, column) > 0 &
						!puzzle.isElementInputAt(row, column)) {
					nonInputValueFound = true;
				}
			}
		}
		String question;
		if (nonInputValueFound) {
			question = "You have values entered that are not input values. These will be overwritten. Are you sure you want the solution now?";
		} else {
			question = "Are you sure you want the solution now?";
		}
		if (askQuestion(question)) {
			for (int row = 0; row < matrixSize; row++) {
				for (int column = 0; column < matrixSize; column++) {
					puzzle.setValueAt(row, column,
							puzzle.getSolutionValueAt(row, column));
				}
			}
			redraw();
		}
	}
	
	/**
	 * Handle user action for a hint. To provie a hint, copy the value of the currently selected 
	 * element from the solution matrix.
	 */
	public static void provideHint(){
		if (currentSelection != null & puzzle.isPuzzleSolved()) {
			puzzle.setValueAt(currentSelection.x, currentSelection.y, 
					puzzle.getSolutionValueAt(currentSelection.x, currentSelection.y));
			redraw();
		}
	}

	/**
	 * Handle user action to display help.
	 */
	public static void help() {
		displayMessage("Help");
	}

	/**
	 * Handle user action for information about the application.
	 */
	public static void about() {
		displayMessage("About Sudoku");
	}

	/**
	 * Create listeners for mouse and keyboard events.
	 */
	public static void createListeners() {
		MouseListener mouseListener = new MouseListener() {

			/**
			 * Add a listener to handle a double click and select the element.
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
				//Get the row and column indices of the element based on the location of the event
				Point mouseLocationInMatrix = getMouseLocationInMatrix(e.x,
						e.y);
				//If the user double clicked within the matrix and did so on an element that is
				//not an input element, select it.
				if (mouseLocationInMatrix != null & 
						!puzzle.isElementInputAt(mouseLocationInMatrix.x, mouseLocationInMatrix.y)) {
					currentSelection = mouseLocationInMatrix;
					redraw();
				}
				setMenuItemEnablement();
			}

			public void mouseDown(MouseEvent e) {
			}

			/**
			 * Add a listener to handle the mouse up event and select an element
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					Point mouseLocationInMatrix = getMouseLocationInMatrix(e.x,
							e.y);
					//If the user clicked within the matrix and did so on an emement that is not an
					//input element, select it.
					if (mouseLocationInMatrix != null & 
							!puzzle.isElementInputAt(mouseLocationInMatrix.x, mouseLocationInMatrix.y)) {
						currentSelection = mouseLocationInMatrix;
						redraw();
					}
				}
				setMenuItemEnablement();
			}
		};
		canvas.addMouseListener(mouseListener);

		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				//Variable to indicate whether or not we need to redraw the matrix.
				boolean needRedraw = false;
				switch (e.keyCode) {
				case SWT.ARROW_UP:
					//If no element is selected, select one in the middle of the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
					//Otherwise move up looking for an element that is not an input element. If at the
					//top of the matrix, start again at the bottom of that column. 
					} else {
						int i = 0;
						do {
							if (currentSelection.x == 0) {
								currentSelection.x = matrixSize - 1;
							} else {
								currentSelection.x = currentSelection.x - 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x, currentSelection.y) & 
								i < matrixSize);
					}
					needRedraw = true;
					break;
				case SWT.ARROW_DOWN:
					//If no element is selected, select one in the middle of the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
					//Otherwise move down looking for an element that is not an input element. If at the
					//bottom of the matrix, start again at the top of that column. 
					} else {
						int i = 0;
						do {
							if (currentSelection.x == matrixSize - 1) {
								currentSelection.x = 0;
							} else {
								currentSelection.x = currentSelection.x + 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x, currentSelection.y) &
								i < matrixSize);
					}
					needRedraw = true;
					break;
				case SWT.ARROW_LEFT:
					//If no element is selected, select one in the middle of the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
					//Otherwise move left looking for an element that is not an input element. If at the
					//left end of the matrix, start again at the right end of that row. 
					} else {
						int i = 0;
						do {
							if (currentSelection.y == 0) {
								currentSelection.y = matrixSize - 1;
							} else {
								currentSelection.y = currentSelection.y - 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x, currentSelection.y) &
								i < matrixSize);
					}
					needRedraw = true;
					break;
				case SWT.ARROW_RIGHT:
					//If no element is selected, select one in the middle of the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
					//Otherwise move right looking for an element that is not an input element. If at the
					//right end of the matrix, start again at the left end of that row
					} else {
						int i = 0;
						do {
							if (currentSelection.y == matrixSize - 1) {
								currentSelection.y = 0;
							} else {
								currentSelection.y = currentSelection.y + 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x, currentSelection.y) &
								i < matrixSize);
					}
					needRedraw = true;
					break;
				//If the user presses 'Delete', clear the value in the current selected element.
				case SWT.DEL:
					if (currentSelection != null) {
						puzzle.clearValueAt(currentSelection.x, currentSelection.y);
						needRedraw = true;
						break;
					}

				//If the user presses back space, clear the value in the current selected element.
				case SWT.BS:
					if (currentSelection != null) {
						puzzle.clearValueAt(currentSelection.x, currentSelection.y);
						needRedraw = true;
						break;
					}
				}
				
				if (currentSelection != null) {
					switch (e.character) {
					//If the user presses the space bar, clear the value in the currently 
					//selected element.
					case ' ':
						puzzle.clearValueAt(currentSelection.x, currentSelection.y);
						needRedraw = true;
						break;
					//If the user presses a number, set it as the value of the currently
					//selected element.	
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						int inputValue = new Integer(new Character(e.character)
								.toString()).intValue();
						puzzle.setValueAt(currentSelection.x, currentSelection.y, inputValue);
						needRedraw = true;
						break;
					}
				}
				if (needRedraw) {
					redraw();
					setMenuItemEnablement();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		};
		canvas.addKeyListener(keyListener);
	}

	/**
	 * Takes coordinates of an event and calculates the row and column index of the 
	 * corresponding element in the matrix. Returns null of the event location is outside the
	 * matrix.
	 * 
	 * @param x X coordinate from an <code>Event</code>
	 * @param y Y coordinate from an <code>Event</code>
	 * @return a Point where x = row and y = column
	 */
	public static Point getMouseLocationInMatrix(int x, int y) {
		Rectangle clientArea = canvas.getClientArea();
		int column = x - (clientArea.width - matrixSize * elementSize) / 2;
		int row = y - (clientArea.height - matrixSize * elementSize) / 2;
		if (row >= 0 & column >= 0) {
			row = row / elementSize;
			column = column / elementSize;
			if (row < matrixSize & column < matrixSize) {
				return new Point(row, column);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Redraw the puzzle
	 */
	private static void redraw() {
		canvas.redraw();
		shell.update();
	}

	/**
	 * Draw the puzzle
	 * 
	 * @param gc <code>GraphicsContext</code>
	 */
	public static void draw(GC gc) {

		gc.setFont(font);
		gc.setLineWidth(majorLineWidth);
		gc.setBackground(backgroundColor);
		drawMatrix(gc);
		if (currentSelection != null) {
			drawCurrentSelection(gc);
		}
		gc.dispose();
	}

	public static void drawMatrix(GC gc) {
		Rectangle clientArea = shell.getClientArea();
		//Draw a rectangle for the shadow of the matrix
		gc.setBackground(shadowColor);
		gc.fillRectangle((clientArea.width - matrixSize * elementSize) / 2
				+ shadowOffset,
				(clientArea.height - matrixSize * elementSize) / 2
						+ shadowOffset, matrixSize * elementSize, matrixSize
						* elementSize);
		//Now draw each element
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				drawValue(gc, row, column);
			}
		}
		//Draw the lines delineating the rows, either major lines or minor lines
		int x1, x2, y1, y2;
		x1 = (clientArea.width - matrixSize * elementSize) / 2;
		x2 = x1 + matrixSize*elementSize;
		for (int row = 0; row < matrixSize + 1; row++) {
			if (row % subMatrixSize == 0) {
				gc.setLineWidth(majorLineWidth);
			} else {
				gc.setLineWidth(minorLineWidth);
			}
			y1 = (clientArea.height - matrixSize * elementSize) / 2 + row
					* elementSize;
			y2 = y1;
			gc.drawLine(x1, y1, x2, y2);
		}
		//Draw the lines delineating the columns, either major or minor lines
		y1 = (clientArea.height - matrixSize * elementSize) / 2;
		y2 = y1 + matrixSize*elementSize;
		for (int column = 0; column < matrixSize + 1; column++) {
			if (column % subMatrixSize == 0) {
				gc.setLineWidth(majorLineWidth);
			} else {
				gc.setLineWidth(minorLineWidth);
			}
			x1 = (clientArea.width - matrixSize * elementSize) / 2 + column
					* elementSize;
			x2 = x1;
			gc.drawLine(x1, y1, x2, y2);
		}
	}

	/**
	 * Draw an element value (row, column)
	 * 
	 * @param gc <code>GraphicsContext</code>
	 * @param row Row index of element to draw
	 * @param column Column index of element to draw
	 */
	public static void drawValue(GC gc, int row, int column) {
		Rectangle clientArea = shell.getClientArea();
		gc.setBackground(matrixColor);

		//Calculate the upper left corner of the rectangle to draw for this element
		int x = (clientArea.width - matrixSize * elementSize) / 2 + column
				* elementSize;
		int y = (clientArea.height - matrixSize * elementSize) / 2 + row
				* elementSize;
		//Now perform the draw operations
		performDrawForValueAt(gc, getMatrixElement(row, column), x, y, 
				puzzle.isElementValidAt(row, column), 
				puzzle.isElementInputAt(row, column));
	}

	/**
	 * Draw an elment
	 * 
	 * @param gc <code>graphicsContext</code>
	 * @param s String representation of numeric value of element
	 * @param x X coordinate of upper right of rectangle where element is drawn
	 * @param y Y coordinate of upper right of rectangle where element is drawn
	 * @param isValidElement Indicates if the element is valid
	 * @param isInputElement Indicates if the element is an input
	 */
	public static void performDrawForValueAt(GC gc, String s, int x, int y,
			boolean isValidElement, boolean isInputElement) {
		//If an input element, color it differently
		if (isInputElement) {
			gc.setBackground(inputColor);
		} else {
			gc.setBackground(matrixColor);
		}
		//Fill the background 
		gc.fillRectangle(x, y, elementSize, elementSize);
		//If the element is invalid, shade it.
		if (!isValidElement) {
			gc.setBackground(errorColor);
			gc.setAlpha(63);
			gc.fillRectangle(x, y, elementSize, elementSize);
			gc.setBackground(matrixColor);
			gc.setAlpha(255);
		}
		//If the element has a value, draw it.
		if (s != null) {
			Point size = gc.textExtent(s);
			gc.drawString(s, x + elementSize / 2 - size.x / 2, y
					+ elementSize / 2 - size.y / 2, true);
		}
	}

	/**
	 * Lastly, draw in the currently selection.
	 * @param gc
	 */
	public static void drawCurrentSelection(GC gc) {
		Rectangle clientArea = shell.getClientArea();
		//Calculate the location of the current selection
		int x = (clientArea.width - matrixSize * elementSize) / 2
				+ (currentSelection.y) * elementSize;
		int y = (clientArea.height - matrixSize * elementSize) / 2
				+ (currentSelection.x) * elementSize;
		//Draw a shadow under the current selection
		gc.setBackground(shadowColor);
		gc.fillRectangle(x, y, elementSize, elementSize);
		//Draw the value and it's background
		performDrawForValueAt(gc, getMatrixElement(currentSelection.x,
				currentSelection.y), x - selectionOffset, y - selectionOffset,
				puzzle.isElementValidAt(currentSelection.x, currentSelection.y), 
				puzzle.isElementInputAt(currentSelection.x, currentSelection.y));
		//Finally, outline the current selection
		gc.setLineWidth(minorLineWidth);
		gc.drawRectangle(x-selectionOffset, y-selectionOffset, elementSize, elementSize);
	}

	/**
	 * Return a string representation of the value of the element
	 * 
	 * @param p Point where X is the row index and Y is the column index
	 * @return String representation of value
	 */
	public static String getMatrixElement(Point p) {
		return getMatrixElement(p.x, p.y);
	}

	/**
	 * Return a string representation of the value of the element
	 * 
	 * @param row Row index of element
	 * @param column Column index of element
	 * @return String representation of value
	 */
	public static String getMatrixElement(int row, int column) {
		int i = puzzle.getValueAt(row, column);
		if (i > 0) {
			return new Integer(i).toString();
		} else {
			return null;
		}
	}
}
