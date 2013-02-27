package org.kellerman;

import sudoku.SudokuPuzzle;
import com.google.gson.Gson;

public class HelloGson {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Gson gson = new Gson();
		String json;
		
/*		System.out.println(args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}
		System.out.println("Json:");
		System.out.println(json);*/
		
		SudokuPuzzle s = new SudokuPuzzle(9);
		s.generateMatrixWithInputValues(50);
		json = gson.toJson(s.getValues());
		System.out.println(json);
		if (s.computeSolutionFromInputs()) {
			System.out.println("Solved puzzle");
			json = gson.toJson(s.getSolutionMatrix().getValues());
			System.out.println(json);
		} else {
			System.out.println("Didn't solve puzzle");
		}
	}
}
