/*
 * $Id: RegisterView.java 317 2012-09-26 13:20:38Z MATPOCKuH $
 */

package ru.ifmo.cs.bcomp.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JLabel;
import ru.ifmo.cs.bcomp.ui.Utils;
import static ru.ifmo.cs.bcomp.ui.components.DisplayStyles.*;

/**
 *
 * @author dima
 */
public class BCompComponent extends JComponent {
	protected int width;
	protected int height;
	protected JLabel title;

	private BCompComponent(String title, int ncells, Color color) {
		this.height = 3 + CELL_HEIGHT * (ncells + 1);

		this.title = addLabel(title, FONT_COURIER_BOLD_21, color);
	}

	public BCompComponent(String title, Color colorTitleBG) {
		this(title, 1, colorTitleBG);
	}

	public BCompComponent(String title, int ncells) {
		this(title, ncells, COLOR_TITLE);
	}

	protected final JLabel addLabel(String value, Font font, Color color) {
		JLabel label = new JLabel(value, JLabel.CENTER);
		label.setFont(font);
		label.setBackground(color);
		label.setOpaque(true);
		add(label);
		return label;
	}

	private final JLabel addValueLabel(String value, Color color) {
		return addLabel(value, FONT_COURIER_BOLD_25, color);
	}

	protected final JLabel addValueLabel(Color color) {
		return addValueLabel("", color);
	}

	protected final JLabel addValueLabel(String value) {
		return addValueLabel(value, COLOR_VALUE);
	}

	protected final JLabel addValueLabel() {
		return addValueLabel("", COLOR_VALUE);
	}

	protected void setBounds(int x, int y, int width) {
		this.width = width;
		setBounds(x, y, width, height);
	}

	protected int getValueY(int n) {
		return 2 + CELL_HEIGHT * (n + 1);
	}

	protected static int getValueY() {
		return 2 + CELL_HEIGHT;
	}

	private int getPixelWidth(int chars) {
		return 2 + FONT_COURIER_BOLD_25_WIDTH * (1 + chars);
	}

	protected int getValueWidth(int width) {
		return getPixelWidth(Utils.getHexWidth(width));
	}

	protected int getValueWidth(int width, boolean hex) {
		return hex ? getValueWidth(width) : getPixelWidth(Utils.getBinaryWidth(width));
	}

	protected void setTitleBounds() {
		title.setBounds(1, 1, width - 2, CELL_HEIGHT);
	}

	protected void setTitle(String title) {
		this.title.setText(title);
		setTitleBounds();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width - 1, height - 1);
		g.drawLine(1, CELL_HEIGHT + 1, width - 2, CELL_HEIGHT + 1);
	}
}