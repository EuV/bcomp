/*
 * $Id$
 */

package ru.ifmo.cs.bcomp.ui.components;

import java.awt.Color;
import java.awt.event.*;
import java.util.EnumMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import ru.ifmo.cs.bcomp.CPU;
import ru.ifmo.cs.bcomp.ControlUnit;
import ru.ifmo.cs.bcomp.StateReg;
import ru.ifmo.cs.bcomp.ui.CLI;
import ru.ifmo.cs.bcomp.ui.GUI;
import static ru.ifmo.cs.bcomp.ui.components.DisplayStyles.*;
import ru.ifmo.cs.elements.DataDestination;
import ru.ifmo.cs.elements.Register;

/**
 *
 * @author Dmitry Afanasiev <KOT@MATPOCKuH.Ru>
 */
public class ComponentManager {
	// XXX: move to DisplayStyles
	// Buttons coordinates
	private static final int buttonsHeight = 30;
	private static final int buttonsSpace = 2;
	private static final int buttonsY = 529;
	// Keyboards register
	private static final int regKeyX = 1;
	private static final int regKeyY = 470;

	private class ButtonProperties {
		private int width;
		private String[] texts;
		private Color[] colors;
		private ActionListener listener;

		public ButtonProperties(int width, String[] texts, Color[] colors, ActionListener listener) {
			this.width = width;
			this.texts = texts;
			this.colors = colors;
			this.listener = listener;
		}

		public int getWidth() {
			return width;
		}

		public String[] getTexts() {
			return texts;
		}

		public Color[] getColors() {
			return colors;
		}

		public ActionListener getListener() {
			return listener;
		}
	}

	private class ButtonsPanel extends JComponent {
		public ButtonsPanel() {
			setBounds(0, buttonsY, FRAME_WIDTH, buttonsHeight + 2 * buttonsSpace);

			int buttonsX = 1;

			buttons = new JButton[buttonProperties.length];

			for (int i = 0; i < buttons.length; i++) {
				buttons[i] = new JButton(buttonProperties[i].getTexts()[0]);
				buttons[i].setForeground(buttonProperties[i].getColors()[0]);
				buttons[i].setFont(FONT_COURIER_PLAIN_12);
				buttons[i].setBounds(buttonsX, 0, buttonProperties[i].getWidth(), buttonsHeight);
				buttonsX += buttonProperties[i].getWidth() + buttonsSpace;
				buttons[i].setFocusable(false);
				buttons[i].addActionListener(buttonProperties[i].getListener());
				add(buttons[i]);
			}
		}
	}
	private Color[] colors = { Color.BLACK, Color.RED };
	private ButtonProperties[] buttonProperties = new ButtonProperties[] {
		new ButtonProperties(135, new String[] { "F4 Ввод адреса" }, colors, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdEnterAddr();
			}
		}),
		new ButtonProperties(115, new String[] { "F5 Запись" }, colors, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdWrite();
			}
		}),
		new ButtonProperties(115, new String[] { "F6 Чтение" }, colors, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdRead();
			}
		}),
		new ButtonProperties(90, new String[] { "F7 Пуск" }, colors, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdStart();
			}
		}),
		new ButtonProperties(135, new String[] { "F8 Продолжение" }, colors, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdContinue();
			}
		}),
		new ButtonProperties(110, new String[] { "F9 Останов", "F9 Работа" }, colors, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdInvertRunState();
			}
		}),
		new ButtonProperties(130, new String[] { "Shift+F9 Такт" }, colors, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdInvertClockState();
			}
		})
	};

	private static final int BUTTON_RUN = 5;
	private static final int BUTTON_CLOCK = 6;
	private JButton[] buttons;
	private ButtonsPanel buttonsPanel = new ButtonsPanel();

	private GUI gui;
	private CPU cpu;
	private MemoryView mem;
	private EnumMap<CPU.Regs, RegisterView> regs = new EnumMap<CPU.Regs, RegisterView>(CPU.Regs.class);
	private volatile BCompPanel activePanel;
	private InputRegisterView activeInput;
	private boolean isActive = false;
	private final long[] delayPeriods = { 0, 1, 5, 10, 25, 50, 100, 1000 };
	private int currentDelay = 3;
	private volatile boolean running = false;
	private final Object lockRun = new Object();

	public ComponentManager(GUI gui) {
		this.gui = gui;
		this.cpu = gui.getCPU();

		gui.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						activeInput.moveLeft();
						break;

					case KeyEvent.VK_RIGHT:
						activeInput.moveRight();
						break;

					case KeyEvent.VK_UP:
						activeInput.invertBit();
						break;

					case KeyEvent.VK_0:
					case KeyEvent.VK_NUMPAD0:
						activeInput.setBit(0);
						break;

					case KeyEvent.VK_1:
					case KeyEvent.VK_NUMPAD1:
						activeInput.setBit(1);
						break;

					// XXX: Must be corrected to support Tab key
					case KeyEvent.VK_N:
						InputRegisterView newInput = activePanel.getNextInputRegister();
						if (activeInput != newInput) {
							activeInput.setActive(false);
							(activeInput = newInput).setActive(true);
						}

					case KeyEvent.VK_F1:
						if (e.isShiftDown())
							cmdAbout();
						break;

					case KeyEvent.VK_F4:
						cmdEnterAddr();
						break;

					case KeyEvent.VK_F5:
						cmdWrite();
						break;

					case KeyEvent.VK_F6:
						cmdRead();
						break;

					case KeyEvent.VK_F7:
						cmdStart();
						break;

					case KeyEvent.VK_F8:
						cmdContinue();
						break;

					case KeyEvent.VK_F9:
						if (e.isShiftDown())
							cmdInvertClockState();
						else
							cmdInvertRunState();
						break;

					case KeyEvent.VK_F10:
						System.exit(0);
						break;

					case KeyEvent.VK_F11:
						cmdPrevDelay();
						break;

					case KeyEvent.VK_F12:
						cmdNextDelay();
						break;
				}
			}
		});

		for (CPU.Regs reg : CPU.Regs.values()) {
			switch (reg) {
				case KEY:
					regs.put(reg, new InputRegisterView((Register)cpu.getRegister(reg)));
					break;

				case STATE:
					regs.put(reg, new StateRegisterView(cpu.getRegister(reg)));
					break;

				default:
					regs.put(reg, new RegisterView(cpu.getRegister(reg)));
			}
		}

		mem = new MemoryView(cpu.getMemory(), "Память", 1, 1);

		cpu.addDestination(23, new DataDestination() {
			@Override
			public void setValue(int value) {
				if (isActive)
					mem.eventRead();
				else
					mem.updateLastAddr();
			}
		});

		cpu.addDestination(24, new DataDestination() {
			@Override
			public void setValue(int value) {
				if (isActive)
					mem.eventWrite();
				else
					mem.updateLastAddr();
			}
		});

		// XXX: move to GUI init()
		Thread bcomp = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					synchronized (lockRun) {
						try {
							lockRun.wait(); 
						} catch (Exception e) { }

						running = true;
						cpu.cont();

						for (;;) {
							if (activePanel != null)
								activePanel.stepStart();
							boolean run = cpu.step();
							if (activePanel != null)
								activePanel.stepFinish();
							if (!run)
								break;
							try {
								Thread.sleep(delayPeriods[currentDelay]);
							} catch (Exception e) {	}
						}

						running = false;
					}
				}
			}
		});
		bcomp.start();
	}

	public void panelActivate(BCompPanel component) {
		activePanel = component;
		activePanel.add(mem);
		activePanel.add(buttonsPanel);

		activeInput = (InputRegisterView)regs.get(CPU.Regs.KEY);
		activeInput.setProperties("Клавишный регистр", regKeyX, regKeyY, false);
		activeInput.setActive(true);
		component.add(activeInput);

		mem.updateMemory();

		cpu.addDestination(18, regs.get(CPU.Regs.ADDR));
		cpu.addDestination(19, regs.get(CPU.Regs.DATA));
		cpu.addDestination(20, regs.get(CPU.Regs.INSTR));
		cpu.addDestination(21, regs.get(CPU.Regs.IP));
		cpu.addDestination(22, regs.get(CPU.Regs.ACCUM));
		cpu.addDestination(23, regs.get(CPU.Regs.DATA));

		isActive = true;

		gui.requestFocusInWindow();
	}

	public void panelDeactivate() {
		cpu.removeDestination(18, regs.get(CPU.Regs.ADDR));
		cpu.removeDestination(19, regs.get(CPU.Regs.DATA));
		cpu.removeDestination(20, regs.get(CPU.Regs.INSTR));
		cpu.removeDestination(21, regs.get(CPU.Regs.IP));
		cpu.removeDestination(22, regs.get(CPU.Regs.ACCUM));
		cpu.removeDestination(23, regs.get(CPU.Regs.DATA));

		isActive = false;
		activePanel = null;
	}

	public RegisterView getRegisterView(CPU.Regs reg) {
		return regs.get(reg);
	}

	public void cmdContinue() {
		if (running)
			return;
		synchronized (lockRun) {
			lockRun.notifyAll();
		}
	}

	private void cmdCPUjump(int label) {
		if (running)
			return;
		cpu.jump(label);
		cmdContinue();
	}

	public void cmdEnterAddr() {
		cmdCPUjump(ControlUnit.LABEL_ADDR);
	}

	public void cmdWrite() {
		cmdCPUjump(ControlUnit.LABEL_WRITE);
	}

	public void cmdRead() {
		cmdCPUjump(ControlUnit.LABEL_READ);
	}

	public void cmdStart() {
		cmdCPUjump(ControlUnit.LABEL_START);
	}

	public void cmdInvertRunState() {
		cpu.invertRunState();
		int state = cpu.getStateValue(StateReg.FLAG_RUN);
		buttons[BUTTON_RUN].setForeground(buttonProperties[BUTTON_RUN].getColors()[state]);
		buttons[BUTTON_RUN].setText(buttonProperties[BUTTON_RUN].getTexts()[state]);
		activePanel.stepFinish();
	}

	public void cmdInvertClockState() {
		cpu.invertClockState();
		boolean state = cpu.getClockState();
		buttons[BUTTON_CLOCK].setForeground(buttonProperties[BUTTON_CLOCK].getColors()[state ? 0 : 1]);
		if (!state)
			cpu.cont();
	}

	public void cmdNextDelay() {
		currentDelay = currentDelay < delayPeriods.length - 1 ? currentDelay + 1 : 0;
	}

	public void cmdPrevDelay() {
		currentDelay = (currentDelay > 0 ? currentDelay : delayPeriods.length) - 1;
	}

	public void cmdAbout() {
		JOptionPane.showMessageDialog(gui,
			"Эмулятор Базовой ЭВМ. Версия r" + CLI.class.getPackage().getImplementationVersion() + 
			"\n\nЗагружена " + gui.getMicroProgramName() + " микропрограмма",
			"О программе", JOptionPane.INFORMATION_MESSAGE);		
	}

	public boolean getRunningState() {
		return running;
	}

	public void activeInputSwitch(InputRegisterView input) {
		if (activeInput == input)
			return;

		activeInput.setActive(false);
		(activeInput = input).setActive(true);
	}
}