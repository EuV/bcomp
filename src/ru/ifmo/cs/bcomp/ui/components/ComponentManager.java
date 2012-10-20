/*
 * $Id$
 */

package ru.ifmo.cs.bcomp.ui.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import ru.ifmo.cs.bcomp.*;
import ru.ifmo.cs.bcomp.ui.GUI;
import static ru.ifmo.cs.bcomp.ui.components.DisplayStyles.*;
import ru.ifmo.cs.elements.DataDestination;
import ru.ifmo.cs.elements.Register;
import ru.ifmo.cs.io.IOCtrl;

/**
 *
 * @author Dmitry Afanasiev <KOT@MATPOCKuH.Ru>
 */
public class ComponentManager {
	private class SignalHandler implements DataDestination {
		private final ControlSignal signal;

		public SignalHandler(ControlSignal signal) {
			this.signal = signal;
		}

		@Override
		public void setValue(int value) {
			openBuses.add(signal);
		}
	}

	private class ButtonProperties {
		private int width;
		private String[] texts;
		private ActionListener listener;

		public ButtonProperties(int width, String[] texts, ActionListener listener) {
			this.width = width;
			this.texts = texts;
			this.listener = listener;
		}

		public int getWidth() {
			return width;
		}

		public String[] getTexts() {
			return texts;
		}

		public ActionListener getListener() {
			return listener;
		}
	}

	private class ButtonsPanel extends JComponent {
		public ButtonsPanel() {
			setBounds(0, BUTTONS_Y, FRAME_WIDTH, BUTTONS_HEIGHT);

			int buttonsX = 1;

			buttons = new JButton[buttonProperties.length];

			for (int i = 0; i < buttons.length; i++) {
				buttons[i] = new JButton(buttonProperties[i].getTexts()[0]);
				buttons[i].setForeground(buttonColors[0]);
				buttons[i].setFont(FONT_COURIER_PLAIN_12);
				buttons[i].setBounds(buttonsX, 0, buttonProperties[i].getWidth(), BUTTONS_HEIGHT);
				buttonsX += buttonProperties[i].getWidth() + BUTTONS_SPACE;
				buttons[i].setFocusable(false);
				buttons[i].addActionListener(buttonProperties[i].getListener());
				add(buttons[i]);
			}
		}
	}

	private Color[] buttonColors = new Color[] { Color.BLACK, Color.RED };
	private ButtonProperties[] buttonProperties = {
		new ButtonProperties(135, new String[] { "F4 Ввод адреса" }, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdEnterAddr();
			}
		}),
		new ButtonProperties(115, new String[] { "F5 Запись" }, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdWrite();
			}
		}),
		new ButtonProperties(115, new String[] { "F6 Чтение" }, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdRead();
			}
		}),
		new ButtonProperties(90, new String[] { "F7 Пуск" }, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdStart();
			}
		}),
		new ButtonProperties(135, new String[] { "F8 Продолжение" }, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdContinue();
			}
		}),
		new ButtonProperties(110, new String[] { "F9 Останов", "F9 Работа" }, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdInvertRunState();
			}
		}),
		new ButtonProperties(130, new String[] { "Shift+F9 Такт" }, new ActionListener() {
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

	private final GUI gui;
	private final BasicComp bcomp;
	private final CPU cpu;
	private final IOCtrl[] ioctrls;
	private final MemoryView mem;
	private final MemoryView micromem;
	private EnumMap<CPU.Reg, RegisterView> regs = new EnumMap<CPU.Reg, RegisterView>(CPU.Reg.class);
	private volatile BCompPanel activePanel;
	private InputRegisterView activeInput;
	private final long[] delayPeriods = { 0, 1, 5, 10, 25, 50, 100, 1000 };
	private int currentDelay = 3;
	private volatile boolean running = false;
	private final Object lockRun = new Object();
	private final Object lockActivePanel = new Object();
	private JCheckBox cucheckbox;
	private volatile boolean cuswitch = false;
	private SignalListener[] listeners;
	private ArrayList<ControlSignal> openBuses = new ArrayList<ControlSignal>();
	private static final ControlSignal[] busSignals = {
		ControlSignal.DATA_TO_ALU,
		ControlSignal.INSTR_TO_ALU,
		ControlSignal.IP_TO_ALU,
		ControlSignal.ACCUM_TO_ALU,
		ControlSignal.STATE_TO_ALU,
		ControlSignal.KEY_TO_ALU,
		ControlSignal.BUF_TO_ADDR,
		ControlSignal.BUF_TO_DATA,
		ControlSignal.BUF_TO_INSTR,
		ControlSignal.BUF_TO_IP,
		ControlSignal.BUF_TO_ACCUM,
		ControlSignal.MEMORY_READ,
		ControlSignal.MEMORY_WRITE,
		ControlSignal.INPUT_OUTPUT,
		ControlSignal.IO0_TSF,
		ControlSignal.IO1_TSF,
		ControlSignal.IO1_OUT,
		ControlSignal.IO2_TSF,
		ControlSignal.IO2_IN,
		ControlSignal.IO3_TSF,
		ControlSignal.IO3_IN,
		ControlSignal.IO3_OUT
	};

	public ComponentManager(GUI gui) {
		this.gui = gui;
		bcomp = gui.getBasicComp();
		cpu = gui.getCPU();
		ioctrls = gui.getIOCtrls();

		for (ControlSignal cs : busSignals)
			bcomp.addDestination(cs, new SignalHandler(cs));

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
						else
							cmdSetIOFlag(1);
						break;

					case KeyEvent.VK_F2:
						cmdSetIOFlag(2);
						break;

					case KeyEvent.VK_F3:
						cmdSetIOFlag(3);
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

		for (CPU.Reg reg : CPU.Reg.values()) {
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

		listeners = new SignalListener[] {
			new SignalListener(regs.get(CPU.Reg.STATE),
				ControlSignal.BUF_TO_STATE_C, ControlSignal.CLEAR_STATE_C, ControlSignal.SET_STATE_C),
			new SignalListener(regs.get(CPU.Reg.ADDR), ControlSignal.BUF_TO_ADDR),
			new SignalListener(regs.get(CPU.Reg.DATA), ControlSignal.BUF_TO_DATA),
			new SignalListener(regs.get(CPU.Reg.INSTR), ControlSignal.BUF_TO_INSTR),
			new SignalListener(regs.get(CPU.Reg.IP), ControlSignal.BUF_TO_IP),
			new SignalListener(regs.get(CPU.Reg.ACCUM),
				ControlSignal.BUF_TO_ACCUM, ControlSignal.IO2_IN, ControlSignal.IO3_IN)		
		};

		mem = new MemoryView(cpu.getMemory(), MEM_X, MEM_Y);
		micromem = new MemoryView(cpu.getMicroMemory(), 711, MEM_Y);

		bcomp.addDestination(ControlSignal.MEMORY_READ, new DataDestination() {
			@Override
			public void setValue(int value) {
				if (activePanel != null)
					mem.eventRead();
				else
					mem.updateLastAddr();
			}
		});

		bcomp.addDestination(ControlSignal.MEMORY_WRITE, new DataDestination() {
			@Override
			public void setValue(int value) {
				if (activePanel != null)
					mem.eventWrite();
				else
					mem.updateLastAddr();
			}
		});

		cucheckbox = new JCheckBox("Работа с МПУУ");
		cucheckbox.setFocusable(false);
		cucheckbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				cuswitch = e.getStateChange() == ItemEvent.SELECTED;
			}
		});
	}

	public void startBComp() {
		Thread bcomp = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					synchronized (lockRun) {
						try {
							lockRun.wait();
						} catch (Exception e) {
							continue;
						}
					}

					running = true;
					cpu.cont();

					for (;;) {
						synchronized (lockActivePanel) {
							if (activePanel != null)
								activePanel.stepStart();
						}

						openBuses.clear();
						boolean run = cpu.step();

						synchronized (lockActivePanel) {
							if (activePanel != null)
								activePanel.stepFinish();
						}

						if (!run)
							break;
						try {
							Thread.sleep(delayPeriods[currentDelay]);
						} catch (Exception e) {	}
					}

					running = false;
				}
			}
		});
		bcomp.start();
	}

	public void panelActivate(BCompPanel component) {
		synchronized (lockActivePanel) {
			activePanel = component;
			setSignalListeners(activePanel.getSignalListeners());
			setSignalListeners(listeners);
		}

		activePanel.add(mem);
		activePanel.add(buttonsPanel);

		activeInput = (InputRegisterView)regs.get(CPU.Reg.KEY);
		activeInput.setProperties("Клавишный регистр", REG_KEY_X, REG_KEY_Y, false);
		activeInput.setActive(true);
		component.add(activeInput);

		mem.updateMemory();

		cuswitch = false;

		gui.requestFocusInWindow();
	}

	public void panelDeactivate() {
		synchronized (lockActivePanel) {
			clearSignalListeners(listeners);
			clearSignalListeners(activePanel.getSignalListeners());
			activePanel = null;
		}
	}

	private void setSignalListeners(SignalListener[] listeners) {
		for (SignalListener listener : listeners)
			for (ControlSignal signal : listener.signals)
				bcomp.addDestination(signal, listener.dest);
	}

	private void clearSignalListeners(SignalListener[] listeners) {
		for (SignalListener listener : listeners)
			for (ControlSignal signal : listener.signals)
				bcomp.removeDestination(signal, listener.dest);
	}

	public RegisterView getRegisterView(CPU.Reg reg) {
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
		if (cuswitch) {
			cpu.jump();
			regs.get(CPU.Reg.MIP).setValue();
		} else
			cmdCPUjump(ControlUnit.LABEL_ADDR);
	}

	public void cmdWrite() {
		if (cuswitch) {
			micromem.updateLastAddr();
			cpu.setMicroMemory();
			micromem.updateMemory();
			regs.get(CPU.Reg.MIP).setValue();
		} else
			cmdCPUjump(ControlUnit.LABEL_WRITE);
	}

	public void cmdRead() {
		if (cuswitch) {
			micromem.eventRead();
			cpu.readMInstr();
			regs.get(CPU.Reg.MIP).setValue();
			regs.get(CPU.Reg.MINSTR).setValue();
		} else
			cmdCPUjump(ControlUnit.LABEL_READ);
	}

	public void cmdStart() {
		cmdCPUjump(ControlUnit.LABEL_START);
	}

	public void cmdInvertRunState() {
		cpu.invertRunState();
		int state = cpu.getStateValue(StateReg.FLAG_RUN);
		buttons[BUTTON_RUN].setForeground(buttonColors[state]);
		buttons[BUTTON_RUN].setText(buttonProperties[BUTTON_RUN].getTexts()[state]);
	}

	public void cmdInvertClockState() {
		cpu.invertClockState();
		boolean state = cpu.getClockState();
		buttons[BUTTON_CLOCK].setForeground(buttonColors[state ? 0 : 1]);
		if (!state)
			cpu.cont();
	}

	public void cmdSetIOFlag(int dev) {
		ioctrls[dev].setFlag();
	}

	public void cmdNextDelay() {
		currentDelay = currentDelay < delayPeriods.length - 1 ? currentDelay + 1 : 0;
	}

	public void cmdPrevDelay() {
		currentDelay = (currentDelay > 0 ? currentDelay : delayPeriods.length) - 1;
	}

	public void cmdAbout() {
		JOptionPane.showMessageDialog(gui,
			"Эмулятор Базовой ЭВМ. Версия r" + GUI.class.getPackage().getImplementationVersion() +
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

	public MemoryView getMicroMemory() {
		return micromem;
	}

	public JCheckBox getMPCheckBox() {
		return cucheckbox;
	}

	public ArrayList<ControlSignal> getActiveSignals() {
		return openBuses;
	}

	public void clearActiveSignals() {
		openBuses.clear();
	}
}
