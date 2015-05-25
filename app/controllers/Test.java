package controllers;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;

public class Test {

	public static int getFontMetrics (String fontName, char testChar, int size){
		Font font = new Font(fontName, Font.PLAIN, size);
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int charWidth = fm.charWidth(testChar);
		System.out.println("Font: " + fontName + " char: [" + testChar + "]" + "\t width: " + charWidth);
		return charWidth;
	}

	public static void main(String[] args) {
		Test.getFontMetrics("Arial", ' ' ,14);
		Test.getFontMetrics("Arial", 'A' ,14);
		Test.getFontMetrics("Arial", 'i' ,14);
		Test.getFontMetrics("Courier New", ' ', 14);
		Test.getFontMetrics("Courier New", 'A', 14);	
		Test.getFontMetrics("Courier New", 'i', 14);	
	}

}
