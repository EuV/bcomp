/*
 * $Id$
 */

package ru.ifmo.cs.bcomp.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;

/**
 *
 * @author Dmitry Afanasiev <KOT@MATPOCKuH.Ru>
 */

public class DisplayStyles {
	private static final FontRenderContext fr = new FontRenderContext(null, true, true);

	// Fonts
	public static final Font FONT_COURIER_PLAIN_12 = new Font("Courier New", Font.PLAIN, 12);
	public static final Font FONT_COURIER_BOLD_23 = new Font("Courier New", Font.BOLD, 23);
	public static final int FONT_COURIER_BOLD_23_WIDTH =
		(int)FONT_COURIER_BOLD_23.getStringBounds("0", fr).getWidth();
	public static final Font FONT_COURIER_BOLD_25 = new Font("Courier New", Font.BOLD, 25);
	public static final int FONT_COURIER_BOLD_25_WIDTH =
		(int)FONT_COURIER_BOLD_25.getStringBounds("0", fr).getWidth();

	// Colors
	public static final Color COLOR_BG_TITLE = new Color(157, 189, 165);
	public static final Color COLOR_BG_VALUE = new Color(219, 249, 235);
}
