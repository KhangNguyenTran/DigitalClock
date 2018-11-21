package DigitalClock;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author vinhk
 */
public class AlarmTimeFilter extends DocumentFilter {

	final String regex = "^(((([0]{0,1}[0-9]{0,1})|([1]{0,1}[0-2]{0,1}))[:]([0-5]{0,1}[0-9]{0,1})\\p{Space}[Aa]{0,1})|((([0]{0,1}[0-9]{0,1})|([1]{0,1}[0-1]{0,1}))[:]([0-5]{0,1}[0-9]{0,1})\\p{Space}[pP]{0,1}))[Mm]?$";

	@Override
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
			throws BadLocationException {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.insert(offset, string);

		if (test(sb.toString())) {
			super.insertString(fb, offset, string, attr);
		} else {
			// warn the user and don't allow the insert
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private boolean test(String text) {
		return text.matches(regex);
	}

	@Override
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
			throws BadLocationException {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.replace(offset, offset + length, text);

		if (test(sb.toString())) {
			super.replace(fb, offset, length, text, attrs);
		} else {
			// warn the user and don't allow the insert
			Toolkit.getDefaultToolkit().beep();
		}

	}

	@Override
	public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.delete(offset, offset + length);

		if (test(sb.toString())) {
			super.remove(fb, offset, length);
		} else {
			// warn the user and don't allow the insert
			Toolkit.getDefaultToolkit().beep();
		}

	}
}
