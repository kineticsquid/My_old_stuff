/*******************************************************************************
 * Copyright 2006 IBM by John Kellerman
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package sudokuview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;
import sudoku.SudokuPuzzle;
import sudokuview.Activator;
import sudokuview.preferences.PreferenceConstants;

public class SudokuView extends ViewPart {
	/**
	 * Size of an element of the matrix when it is drawn.
	 */
	int matrixElementSize = 40;

	/**
	 * Distance between the drawn matrix and the edges of the client area
	 */
	int margin = 20;

	/**
	 * Offset, in x and y dimensions, to draw shadows for the matrix
	 */
	int shadowOffset = 10;

	/**
	 * Offset, in x and y dimensions, to draw the currently selected element
	 */
	int selectionOffset = 6;

	/**
	 * Width of major lines of the matrix. The major lines are the outside edges
	 * and the the edges between the submatrices.
	 */
	int majorLineWidth = 2;

	/**
	 * Width of the minor lines of the matrix. The minor lines are the lines
	 * within the submatrices
	 */
	int minorLineWidth = 1;

	/**
	 * Constants to hold color values.
	 */
	static Color backgroundColor, matrixColor, shadowColor, errorColor,
			inputColor;

	/**
	 * Size of the puzzle. I.e. the number of rows and columns.
	 */
	int matrixSize = 9;

	/**
	 * Size of a submatrix. This is {@link #matrixSize } / 3.
	 */
	int subMatrixSize;

	/**
	 * FontData for font used to draw the element values.
	 */
	FontData fontData;
	
	/**
	 * Font used to draw element values.
	 */
	Font font;
	
	/**
	 * RGB used to create the color of the matrix.
	 */
	RGB matrixColorRGB;
	
	/**
	 * RGB used to create to color of the view background.
	 */
	RGB backgroundColorRGB;
	
	/**
	 * RGB used to create to color of shading of error elements.
	 */
	RGB errorColorRGB;
	
	/**
	 * RGB used to create to color of the input elements.
	 */
	RGB inputColorRGB;
	
	/**
	 * String representing whether the puzzle to generate should be blank or have
	 * defined input values
	 */
	int puzzleToGenerate;
	
	/**
	 * String representing level of difficulty of generated puzzle
	 */
	int difficultyLevel;

	/**
	 * Canvas for drawing the puzzle
	 */
	Canvas canvas;

	/**
	 * Represents the currently selected element.
	 */
	Point currentSelection;

	/**
	 * Puzzle being solved.
	 */
	SudokuPuzzle puzzle;

	/**
	 * Fields for the UI actions so that they can be enabled and disabled based on state information
	 */
	Action newAction, lockAction, solveAction, hintAction;
	
	/**
	 * Field to track if we're in the process of handling a request by the user to solve the puzzle.
	 */
	boolean handlingSolveAction = false;

	public SudokuView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		retrievePreferences();
		createANewPuzzle();
		makeActions();
		createDrawingArea(parent);
		createListeners();
		createPropertyChangeListeners();
		setActionEnablement();
	}

	/**
	 * Add a canvas to draw the puzzle on and hook in the context help
	 * @param parent parent widget
	 */
	public void createDrawingArea(Composite parent) {
		canvas = new Canvas(parent, SWT.EMBEDDED | SWT.DOUBLE_BUFFERED);
		IWorkbenchHelpSystem hs = PlatformUI.getWorkbench().getHelpSystem(); 
		hs.setHelp(canvas, "SudokuHelp.main");
	}
	
	/**
	 * Retrieve the preference settings
	 */
	public void retrievePreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		matrixSize = prefs.getInt(PreferenceConstants.SUDOKU_MATRIX_SIZE);
		difficultyLevel = prefs.getInt(PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL);
		puzzleToGenerate = prefs.getInt(PreferenceConstants.SUDOKU_NEW_PUZZLE);

		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		matrixColorRGB = colorRegistry.getRGB(PreferenceConstants.SUDOKU_MATRIX_COLOR);
		backgroundColorRGB = colorRegistry.getRGB(PreferenceConstants.SUDOKU_BACKGROUND_COLOR);
		errorColorRGB = colorRegistry.getRGB(PreferenceConstants.SUDOKU_ERROR_COLOR);
		inputColorRGB = colorRegistry.getRGB(PreferenceConstants.SUDOKU_INPUT_COLOR);
		
		FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		String fontDataString = fontRegistry.getFontData(PreferenceConstants.SUDOKU_PUZZLE_FONT)[0].toString();
		fontData = new FontData(fontDataString);
	}
	
	/**
	 * Create the property change listeners for notification when one of the preferences is
	 * changed by the user
	 */
	public void createPropertyChangeListeners() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String prop = event.getProperty();
				if (prop.equals(PreferenceConstants.SUDOKU_MATRIX_SIZE)) {
					// Do nothing, this takes effect only when a new puzzle is created.
				}
				if (prop.equals(PreferenceConstants.SUDOKU_NEW_PUZZLE)) {
					// Do nothing, this takes effect only when a new puzzle is generated.
				}
				if (prop.equals(PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL)) {
					// Do nothing, this takes effect only when a new puzzle is generated.
				}
			}
		});

		JFaceResources.getFontRegistry().addListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String prop = event.getProperty();
				if (prop.equals(PreferenceConstants.SUDOKU_PUZZLE_FONT)) {
					fontData = new FontData(JFaceResources.getFontRegistry().
							getFontData(prop)[0].toString());
					redraw();
				}
			}
		});
		
		JFaceResources.getColorRegistry().addListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String prop = event.getProperty();
				if (prop.equals(PreferenceConstants.SUDOKU_BACKGROUND_COLOR)) {
					backgroundColorRGB = JFaceResources.getColorRegistry().getRGB(prop);
					redraw();
				}
				if (prop.equals(PreferenceConstants.SUDOKU_MATRIX_COLOR)) {
					matrixColorRGB = JFaceResources.getColorRegistry().getRGB(prop);
					redraw();
				}
				if (prop.equals(PreferenceConstants.SUDOKU_ERROR_COLOR)) {
					errorColorRGB = JFaceResources.getColorRegistry().getRGB(prop);
					redraw();
				}
				if (prop.equals(PreferenceConstants.SUDOKU_INPUT_COLOR)) {
					inputColorRGB = JFaceResources.getColorRegistry().getRGB(prop);
					redraw();
				}
			}
		});
	}

	/**
	 * Create the actions for the user interface
	 */
	public void makeActions() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		IToolBarManager toolBarManager = getViewSite().getActionBars()
				.getToolBarManager();
		
		//newAction creates a new puzzle
		newAction = new Action() {
			public void run() {
				handleNewPuzzle();
			}
		};
		newAction.setText("New Puzzle\tN");
		newAction.setToolTipText("New puzzle");
		newAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_NEW_WIZARD));
		newAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
		menuManager.add(newAction);
		toolBarManager.add(newAction);

		//lockAction toggles the lock on the input matrix elements
		lockAction = new Action("Lock Inputs\tL", Action.AS_CHECK_BOX) {
			public void run() {
				handleToggleLockMatrixInput();
			}
		};
		lockAction.setText("Lock Inputs\tL");
		lockAction
				.setToolTipText("Lock inputs");
		lockAction.setImageDescriptor(ImageDescriptor.createFromFile(SudokuView.class, "icons/lock.png"));
		lockAction.setDisabledImageDescriptor(ImageDescriptor.createFromFile(SudokuView.class, "icons/lock_disabled.png"));
		menuManager.add(lockAction);
		toolBarManager.add(lockAction);

		//hintAction provides a hint. It fills in the currently selected element with the correct value
		//from the solution
		hintAction = new Action() {
			public void run() {
				handleProvideHint();
			}
		};
		hintAction.setText("Hint\tH");
		hintAction
				.setToolTipText("Hint");
		hintAction.setImageDescriptor(ImageDescriptor.createFromFile(SudokuView.class, "icons/help.png"));
		hintAction.setDisabledImageDescriptor(ImageDescriptor.createFromFile(SudokuView.class, "icons/help_disabled.png"));
		menuManager.add(hintAction);
		toolBarManager.add(hintAction);

		//solveAction copies the values from the solution matrix to the input matrix for display to the user
		solveAction = new Action() {
			public void run() {
				handleSolvePuzzle();
			}
		};
		solveAction.setText("Solve Puzzle\tS");
		solveAction.setToolTipText("Solve");
		solveAction.setImageDescriptor(ImageDescriptor.createFromFile(SudokuView.class, "icons/calculator.png"));
		solveAction.setDisabledImageDescriptor(ImageDescriptor.createFromFile(SudokuView.class, "icons/calculator_disabled.png"));
		menuManager.add(solveAction);
		toolBarManager.add(solveAction);
	}

	/**
	 * Create listeners for mouse and keyboard events.
	 */
	public void createListeners() {
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				draw(e.gc);
			}
		});
		canvas.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				handleControlEvent();
			}

			public void controlResized(ControlEvent e) {
				handleControlEvent();
			}
		});

		MouseListener mouseListener = new MouseListener() {

			/**
			 * Add a listener to handle a double click and select the element.
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
				// Get the row and column indices of the element based on the
				// location of the event
				Point mouseLocationInMatrix = getMouseLocationInMatrix(e.x, e.y);
				// If the user double clicked within the matrix and did so on an
				// element that is
				// not an input element, select it.
				if (mouseLocationInMatrix != null
						& !puzzle.isElementInputAt(mouseLocationInMatrix.x,
								mouseLocationInMatrix.y)) {
					currentSelection = mouseLocationInMatrix;
					redraw();
				}
				// setMenuItemEnablement();
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
					// If the user clicked within the matrix and did so on an
					// emement that is not an
					// input element, select it.
					if (mouseLocationInMatrix != null
							& !puzzle.isElementInputAt(mouseLocationInMatrix.x,
									mouseLocationInMatrix.y)) {
						currentSelection = mouseLocationInMatrix;
						redraw();
					}
				}
				// setMenuItemEnablement();
			}
		};
		canvas.addMouseListener(mouseListener);

		KeyListener keyListener = new KeyListener() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				// needRedraw indicates whether or not we need to redraw the
				// matrix.
				boolean needRedraw = false;
				boolean solutionFound = false;
				switch (e.keyCode) {
				case SWT.ARROW_UP:
					// If no element is selected, select one in the middle of
					// the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
						// Otherwise move up looking for an element that is not
						// an input element. If at the
						// top of the matrix, start again at the bottom of that
						// column.
					} else {
						int i = 0;
						do {
							if (currentSelection.x == 0) {
								currentSelection.x = matrixSize - 1;
							} else {
								currentSelection.x = currentSelection.x - 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x,
								currentSelection.y)
								& i < matrixSize);
					}
					needRedraw = true;
					break;
				case SWT.ARROW_DOWN:
					// If no element is selected, select one in the middle of
					// the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
						// Otherwise move down looking for an element that is
						// not an input element. If at the
						// bottom of the matrix, start again at the top of that
						// column.
					} else {
						int i = 0;
						do {
							if (currentSelection.x == matrixSize - 1) {
								currentSelection.x = 0;
							} else {
								currentSelection.x = currentSelection.x + 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x,
								currentSelection.y)
								& i < matrixSize);
					}
					needRedraw = true;
					break;
				case SWT.ARROW_LEFT:
					// If no element is selected, select one in the middle of
					// the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
						// Otherwise move left looking for an element that is
						// not an input element. If at the
						// left end of the matrix, start again at the right end
						// of that row.
					} else {
						int i = 0;
						do {
							if (currentSelection.y == 0) {
								currentSelection.y = matrixSize - 1;
							} else {
								currentSelection.y = currentSelection.y - 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x,
								currentSelection.y)
								& i < matrixSize);
					}
					needRedraw = true;
					break;
				case SWT.ARROW_RIGHT:
					// If no element is selected, select one in the middle of
					// the matrix
					if (currentSelection == null) {
						currentSelection = new Point(matrixSize / 2,
								matrixSize / 2);
						// Otherwise move right looking for an element that is
						// not an input element. If at the
						// right end of the matrix, start again at the left end
						// of that row
					} else {
						int i = 0;
						do {
							if (currentSelection.y == matrixSize - 1) {
								currentSelection.y = 0;
							} else {
								currentSelection.y = currentSelection.y + 1;
							}
							i++;
						} while (puzzle.isElementInputAt(currentSelection.x,
								currentSelection.y)
								& i < matrixSize);
					}
					needRedraw = true;
					break;
				// If the user presses 'Delete', clear the value in the current
				// selected element.
				case SWT.DEL:
					if (currentSelection != null) {
						puzzle.clearValueAt(currentSelection.x,
								currentSelection.y);
						needRedraw = true;
						break;
					}

					// If the user presses back space, clear the value in the
					// current selected element.
				case SWT.BS:
					if (currentSelection != null) {
						puzzle.clearValueAt(currentSelection.x,
								currentSelection.y);
						needRedraw = true;
						break;
					}
				}

				if (currentSelection != null) {
					switch (e.character) {
					// If the user presses the space bar, clear the value in the
					// currently selected element.
					case ' ':
						puzzle.clearValueAt(currentSelection.x,
								currentSelection.y);
						needRedraw = true;
						break;
					// If the user presses a number, set it as the value of the
					// currently selected element.
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
						if (inputValue <= matrixSize) {
							puzzle.setValueAt(currentSelection.x,
									currentSelection.y, inputValue);
							needRedraw = true;
							if (puzzle.isMatrixComplete() & puzzle.isMatrixValid()) {
								solutionFound = true;
							}
						}
						break;
					}	
				}
				switch (e.character) {
				case 'n':
				case 'N':
					if (newAction.isEnabled()) {
						handleNewPuzzle();
					}
					break;
				case 'l':
				case 'L':
					if (lockAction.isEnabled()) {
						lockAction.setChecked(!lockAction.isChecked());
						handleToggleLockMatrixInput();
					}
					break;
				case 's':
				case 'S':
					if (solveAction.isEnabled()) {
						handleSolvePuzzle();
					}
					break;
				case 'h':
				case 'H':
					if (hintAction.isEnabled()) {
						handleProvideHint();
					}
					break;
				}
				if (needRedraw) {
					redraw();
					setActionEnablement();
					if (solutionFound) {
						displayMessage("Congratulations!");
					}

				}
			}

			public void keyReleased(KeyEvent e) {
			}
		};
		canvas.addKeyListener(keyListener);
	}

	/**
	 * Resize and redraw as required due to view movement and resizing
	 */
	public void handleControlEvent() {
		canvas.setLocation(canvas.getParent().getLocation());
		canvas.setSize(canvas.getParent().getClientArea().width, canvas
				.getParent().getClientArea().height);
		redraw();
	}

	/**
	 * Takes coordinates of an event and calculates the row and column index of
	 * the corresponding element in the matrix. Returns null of the event
	 * location is outside the matrix.
	 * 
	 * @param x
	 *            X coordinate from an <code>Event</code>
	 * @param y
	 *            Y coordinate from an <code>Event</code>
	 * @return a Point where x = row and y = column
	 */
	public Point getMouseLocationInMatrix(int x, int y) {
		Rectangle clientArea = canvas.getClientArea();
		int column = x - (clientArea.width - matrixSize * matrixElementSize) / 2;
		int row = y - (clientArea.height - matrixSize * matrixElementSize) / 2;
		if (row >= 0 & column >= 0) {
			row = row / matrixElementSize;
			column = column / matrixElementSize;
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
	 * Causes the puzzle to be redrawn 
	 */
	public void redraw() {
		canvas.redraw();
	}

	/**
	 * Draw the puzzle
	 * 
	 * @param gc
	 *            <code>GraphicsContext</code>
	 */
	public void draw(GC gc) {
		int minDimension;
		if (canvas.getClientArea().width < canvas.getClientArea().height) {
			minDimension = canvas.getClientArea().width;
		} else {
			minDimension = canvas.getClientArea().height;
		}
		matrixElementSize = minDimension / (matrixSize + 3);
		margin = matrixElementSize * 3 / 2;
		shadowOffset = matrixElementSize / 4;
		selectionOffset = shadowOffset * 2 / 3;
		createColorsAndFonts(gc);
		canvas.setBackground(backgroundColor);
		gc.setLineWidth(majorLineWidth);
		gc.setBackground(backgroundColor);
		gc.setFont(font);
		drawMatrix(gc);
		if (currentSelection != null) {
			drawCurrentSelection(gc);
		}
		disposeColorsAndFonts();
		handlingSolveAction = false;
	}

	/**
	 * Do as the name states
	 */
	public void disposeColorsAndFonts() {
		font.dispose();
		matrixColor.dispose();
		backgroundColor.dispose();
		inputColor.dispose();
		errorColor.dispose();
	}

	/**
	 * Do as the name states
	 * @param gc
	 *            <code>GraphicsContext</code>
	 */
	public void createColorsAndFonts(GC gc) {
		Display display = canvas.getDisplay();
		matrixColor = new Color(gc.getDevice(), matrixColorRGB);
		backgroundColor = new Color(gc.getDevice(), backgroundColorRGB);
		shadowColor = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
		errorColor = new Color(gc.getDevice(), errorColorRGB);
		inputColor = new Color(gc.getDevice(), inputColorRGB);
		fontData.setHeight(matrixElementSize / 2);
		font = new Font(display, fontData);
	}

	/**
	 * Draw the puzzle
	 * @param gc
	 *            <code>GraphicsContext</code>
	 */
	public void drawMatrix(GC gc) {
		Rectangle clientArea = canvas.getClientArea();
		// Draw a rectangle for the shadow of the matrix
		gc.setBackground(shadowColor);
		gc.fillRectangle((clientArea.width - matrixSize * matrixElementSize) / 2
				+ shadowOffset, (clientArea.height - matrixSize * matrixElementSize)
				/ 2 + shadowOffset, matrixSize * matrixElementSize, matrixSize
				* matrixElementSize);
		// Now draw each element
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				drawValue(gc, row, column);
			}
		}
		// Draw the lines delineating the rows, either major lines or minor
		// lines
		int x1, x2, y1, y2;
		x1 = (clientArea.width - matrixSize * matrixElementSize) / 2;
		x2 = x1 + matrixSize * matrixElementSize;
		for (int row = 0; row < matrixSize + 1; row++) {
			if (row % subMatrixSize == 0) {
				gc.setLineWidth(majorLineWidth);
			} else {
				gc.setLineWidth(minorLineWidth);
			}
			y1 = (clientArea.height - matrixSize * matrixElementSize) / 2 + row
					* matrixElementSize;
			y2 = y1;
			gc.drawLine(x1, y1, x2, y2);
		}
		// Draw the lines delineating the columns, either major or minor lines
		y1 = (clientArea.height - matrixSize * matrixElementSize) / 2;
		y2 = y1 + matrixSize * matrixElementSize;
		for (int column = 0; column < matrixSize + 1; column++) {
			if (column % subMatrixSize == 0) {
				gc.setLineWidth(majorLineWidth);
			} else {
				gc.setLineWidth(minorLineWidth);
			}
			x1 = (clientArea.width - matrixSize * matrixElementSize) / 2 + column
					* matrixElementSize;
			x2 = x1;
			gc.drawLine(x1, y1, x2, y2);
		}
	}

	/**
	 * Draw an element value (row, column)
	 * 
	 * @param gc
	 *            <code>GraphicsContext</code>
	 * @param row
	 *            Row index of element to draw
	 * @param column
	 *            Column index of element to draw
	 */
	public void drawValue(GC gc, int row, int column) {
		Rectangle clientArea = canvas.getClientArea();
		gc.setBackground(matrixColor);
		// Calculate the upper left corner of the rectangle to draw for this
		// element
		int x = (clientArea.width - matrixSize * matrixElementSize) / 2 + column
				* matrixElementSize;
		int y = (clientArea.height - matrixSize * matrixElementSize) / 2 + row
				* matrixElementSize;
		// Now perform the draw operations. 
		if (handlingSolveAction) {
			//If we are processing a solve request from the 
			// user, check to see if the value in the solution matrix differs from the value in
			// the input matrix. This means what the user entered is incorrect, so put the 
			// correct value in the input matrix and then mark it as incorrect to show the user that
			// his/her value was incorrect.
			boolean errorElement = false;
			if (puzzle.getValueAt(row, column) > 0 &
					puzzle.getValueAt(row, column) != puzzle.getSolutionValueAt(row, column)) {
				errorElement = true;
			}
			puzzle.setValueAt(row, column, puzzle.getSolutionValueAt(row, column));
			performDrawForValueAt(
					gc, 
					getMatrixElement(row, column), 
					x, 
					y, 
					!errorElement,
					puzzle.isElementInputAt(row, column));
		} else {
			// Otherwise, simply draw this element
			performDrawForValueAt(
					gc, 
					getMatrixElement(row, column), 
					x, 
					y, 
					puzzle.isElementValid(row, column),
					puzzle.isElementInputAt(row, column));
		}
	}
	/**
	 * Draw an elment
	 * 
	 * @param gc
	 *            <code>graphicsContext</code>
	 * @param s
	 *            String representation of numeric value of element
	 * @param x
	 *            X coordinate of upper right of rectangle where element is
	 *            drawn
	 * @param y
	 *            Y coordinate of upper right of rectangle where element is
	 *            drawn
	 * @param isValidElement
	 *            Indicates if the element is valid
	 * @param isInputElement
	 *            Indicates if the element is an input
	 */
	public void performDrawForValueAt(GC gc, String s, int x, int y,
			boolean isValidElement, boolean isInputElement) {
		// If an input element, color it differently
		if (isInputElement) {
			gc.setBackground(inputColor);
		} else {
			gc.setBackground(matrixColor);
		}
		// Fill the background
		gc.fillRectangle(x, y, matrixElementSize, matrixElementSize);
		// If the element is invalid, shade it.
		if (!isValidElement) {
			gc.setBackground(errorColor);
			gc.setAlpha(63);
			gc.fillRectangle(x, y, matrixElementSize, matrixElementSize);
			gc.setBackground(matrixColor);
			gc.setAlpha(255);
		}
		// If the element has a value, draw it.
		if (s != null) {
			Point size = gc.textExtent(s);
			gc.drawString(s, x + matrixElementSize / 2 - size.x / 2, y + matrixElementSize
					/ 2 - size.y / 2, true);
		}
	}

	/**
	 * Lastly, draw in the currently selection.
	 * 
	 * @param gc
	 */
	public void drawCurrentSelection(GC gc) {
		Rectangle clientArea = canvas.getClientArea();
		// Calculate the location of the current selection
		int x = (clientArea.width - matrixSize * matrixElementSize) / 2
				+ (currentSelection.y) * matrixElementSize;
		int y = (clientArea.height - matrixSize * matrixElementSize) / 2
				+ (currentSelection.x) * matrixElementSize;
		// Draw a shadow under the current selection
		gc.setBackground(shadowColor);
		gc.fillRectangle(x, y, matrixElementSize, matrixElementSize);
		// Draw the value and it's background
		performDrawForValueAt(
				gc,
				getMatrixElement(currentSelection.x, currentSelection.y),
				x - selectionOffset,
				y - selectionOffset,
				puzzle.isElementValid(currentSelection.x, currentSelection.y),
				puzzle.isElementInputAt(currentSelection.x, currentSelection.y));
		// Finally, outline the current selection
		gc.setLineWidth(minorLineWidth);
		gc.drawRectangle(x - selectionOffset, y - selectionOffset, matrixElementSize,
				matrixElementSize);
	}

	/**
	 * Return a string representation of the value of the element
	 * 
	 * @param row
	 *            Row index of element
	 * @param column
	 *            Column index of element
	 * @return String representation of value
	 */
	public String getMatrixElement(int row, int column) {
		int i = puzzle.getValueAt(row, column);
		if (i > 0) {
			return new Integer(i).toString();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		canvas.setFocus();
	}

	/**
	 * Handle user action to create a new puzzle.
	 */
	public void handleNewPuzzle() {
		if (askQuestion("Are you sure you want to quit this puzzle and start a new one?")) {
			handlingSolveAction = false;
			createANewPuzzle();
			currentSelection = null;
			setActionEnablement();
			redraw();
		}
	}
	
	/**
	 * Handle the user action to create a new puzzle. Need to retrieve some preferences which may have 
	 * changed.
	 */
	private void createANewPuzzle() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		matrixSize = new Integer(prefs.getString(PreferenceConstants.SUDOKU_MATRIX_SIZE)).intValue();
		int newPuzzleType = new Integer(prefs.getString(PreferenceConstants.SUDOKU_NEW_PUZZLE)).intValue();
		puzzle = new SudokuPuzzle(matrixSize);
		subMatrixSize = puzzle.getSubMatrixSize();
		if (newPuzzleType == PreferenceConstants.SUDOKU_NEW_PUZZLE_GENERATED) {
			// If we are going to generate a puzzle for the user, get level of difficulty and use this to
			//specify the desired number of blank elements in the generated input matrix
			int difficulty = new Integer(prefs.getString(PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL)).intValue();
			if (difficulty == PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_EASY) {
				puzzle.generateMatrixWithInputValues(matrixSize*matrixSize/2);
			} else {
				if (difficulty == PreferenceConstants.SUDOKU_DIFFICULTY_LEVEL_MEDIUM) {
					puzzle.generateMatrixWithInputValues(matrixSize*matrixSize*3/5);
				} else {
					puzzle.generateMatrixWithInputValues(matrixSize*matrixSize*2/3);
				}
			}
		}
	}

	/**
	 * Handle user action to toggle the locking of elements, with values defined, as inputs. 
	 */
	public void handleToggleLockMatrixInput() {
		// If the inputs are locked, unlock them.
		if (puzzle.isInputsLocked()) {
			puzzle.unlockInputValues();
			setActionEnablement();
			redraw();
		// Else, handle locking the inputs	
		} else {
			// Attempt to solve the puzzle.
			boolean solved = puzzle.computeSolutionFromInputs();
			// If the puzzle can be solved, lock the input values
			if (solved) {
				puzzle.lockInputValues();
				// Otherwise matrix can't be solved with currently defined values.
			} else {
				displayMessage("Puzzle not solvable. Check your input values.");
			}
			setActionEnablement();
			redraw();
		}
	}

	/**
	 * Handle user action to solve the puzzle. Prompt the user and then copy the
	 * solution values to the matrix.
	 */
	public void handleSolvePuzzle() {
		// First check to see if the user has entered values in the matrix since
		// locking the
		// inputs. If so, these values will be ovewritten. This is because the
		// solution was generated
		// when the inputs were locked. Computing the solution everytime the
		// user enters or changes
		// an input is not feasible.
		boolean nonInputValueFound = false;
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				if (puzzle.getValueAt(row, column) > 0
						& !puzzle.isElementInputAt(row, column)) {
					nonInputValueFound = true;
				}
			}
		}
		String question;
		if (nonInputValueFound) {
			question = "You have values entered that are not input values. These will be overwritten if they are incorrect. Are you sure you want the solution now?";
		} else {
			question = "Are you sure you want the solution now?";
		}
		if (askQuestion(question)) {
			currentSelection = null;
			handlingSolveAction = true;
			redraw();
		}
	}

	/**
	 * Handle user action for a hint. To provie a hint, copy the value of the
	 * currently selected element from the solution matrix.
	 */
	public void handleProvideHint() {
		if (currentSelection != null & puzzle.isSolutionKnown()) {
			puzzle.setValueAt(currentSelection.x, currentSelection.y,
					puzzle.getSolutionValueAt(currentSelection.x,currentSelection.y));
			redraw();
			if (puzzle.isMatrixComplete() & puzzle.isMatrixValid()) {
				displayMessage("Congratulations!");
			}
		}
	}

	/**
	 * This method is called whenever there is a state change in the puzzle that
	 * can affect which menuItems should be enabled.
	 */
	public void setActionEnablement() {
		// If a value has been entered, lock the entered values to be locked as inputs. 
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
		if (inputValueFound) {
			lockAction.setEnabled(true);
		} else {
			lockAction.setEnabled(false);
		}
		// If an element is selected and we have a solution
		if (currentSelection != null & puzzle.isSolutionKnown()) {
			// If the current select is not an input element, allow a hint to be
			// generated. Since the
			// navigation scheme prevents selecting an input element, this check
			// is here to handle the
			// case in which the user locks the inputs (and the current
			// selection is an input element
			// and so remains selected) and immediately asks for a hint.
			if (!puzzle.isElementInputAt(currentSelection.x, currentSelection.y)) {
				hintAction.setEnabled(true);
				// Else prevent a hint from being generated
			} else {
				hintAction.setEnabled(false);
			}
			// Else an element is not selected, so prevent a hint from being
			// generated.
		} else {
			hintAction.setEnabled(false);
		}
		// If a solution exists for the puzzle, allow the user to ask for the
		// solution. Otherwise not.
		if (puzzle.isSolutionKnown()) {
			solveAction.setEnabled(true);
		} else {
			solveAction.setEnabled(false);
		}
	}

	/**
	 * Helper method to display a message to the user.
	 * 
	 * @param msg
	 *            Message to display.
	 */
	public void displayMessage(String msg) {
		MessageDialog.openInformation(
				getSite().getShell(),
				"Sudoku",
				msg);
	}

	/**
	 * Ask the user a question. Return true if the user clicks on 'OK', false
	 * otherwise.
	 * 
	 * @param msg
	 *            Question to ask
	 * @return True if the user clicks on 'OK', false otherwise.
	 */
	public boolean askQuestion(String msg) {
		if (MessageDialog.openQuestion(getSite().getShell(), "Sudoku", msg)) {
			return true;
		} else
			return false;
	}
}
