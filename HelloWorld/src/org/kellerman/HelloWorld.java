package org.kellerman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HelloWorld {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Arguments:");
		for (int i = 0; i < args.length; i++) {
			System.out.print(i + ": ");
			System.out.println(args[i]);
		}

		String someText = null;

		while (someText != "") {

			System.out.println("Enter some text:");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));

			try {
				someText = br.readLine();
			} catch (IOException ioe) {
				System.out.println("IO error trying to read text.");
				System.exit(1);
			}

			System.out.println("Here's what you entered: " + someText);
		}
	}

}
