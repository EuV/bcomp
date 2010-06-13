import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import Machine.EControlView;
import Machine.EMachine;

public class EUI extends JApplet
{	
	//final EMachine machine = new EMachine();
	public void init ()
	{
		
		EventQueue.invokeLater(new Runnable()
		{
		public void run()
		{	 
			final JTabbedPane tabbedPane = new JTabbedPane();
			
			EControlView c = new EControlView(tabbedPane);
			c.SetTact();
			final EMachine machine = new EMachine(c);
			
			EUIBasePCFactory factory = new EUIBasePCFactory(machine);
			
			final EUIBasePC BasePC = new EUIBasePC(factory);
			EUIOutputPC OutputPC = new EUIOutputPC(factory);
			final EUIMicroPC MicroPC = new EUIMicroPC(factory);
			
			final JPanel finalpanel = new JPanel();
			finalpanel.setLayout(null);
			
			
			
			
			tabbedPane.addTab("Базовая ЭВМ", BasePC);
			tabbedPane.addTab("Ввод/Вывод", OutputPC);
	        tabbedPane.addTab("Работа с МК", MicroPC);
	        tabbedPane.setSize(852, 550);
	        tabbedPane.setFocusable(false);
	        finalpanel.add(tabbedPane);
	        finalpanel.setFocusable(true);
       
	        finalpanel.setBackground(Color.WHITE);
	        add(finalpanel);
	        
	        
	        
	        finalpanel.addKeyListener(new KeyAdapter() {
	        	public void keyPressed(KeyEvent e) 
		        {     
			        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
	        		{
		        		BasePC.SetPointerPosition(BasePC.GetPointerPosition() + 1);
		        		tabbedPane.repaint();
	        		}
			       
			        if (e.getKeyCode() == KeyEvent.VK_LEFT)
	        		{
		        		BasePC.SetPointerPosition(BasePC.GetPointerPosition() - 1);
		        		tabbedPane.repaint();
	        		}
			        
			        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN)
	        		{
		        		BasePC.SetBit();
		        		tabbedPane.repaint();
	        		}
			        
			        if (e.getKeyCode() == KeyEvent.VK_0)
	        		{
		        		BasePC.SetBit(false);
		        		tabbedPane.repaint();
	        		}
			        
			        if (e.getKeyCode() == KeyEvent.VK_1)
	        		{
		        		BasePC.SetBit(true);
		        		tabbedPane.repaint();
	        		}
			        
			        if (e.getKeyCode() == KeyEvent.VK_F4)
			        {
		        		machine.Adress();
		        		tabbedPane.repaint();
	        		}
			        
			        if (e.getKeyCode() == KeyEvent.VK_F5)
			        {
		        		machine.Record();
		        		tabbedPane.repaint();
	        		}

			        if (e.getKeyCode() == KeyEvent.VK_F7)
			        {
		        		machine.StopWork();
		        		tabbedPane.repaint();
	        		}
			        
			        if (e.getKeyCode() == KeyEvent.VK_F8)
			        {
				        Runnable r = new MachineRunnable(machine);
				        final Thread t = new Thread(r);
			        	t.start();
		        		tabbedPane.repaint();
	        		}

		        }
			});	        

		}
	});
	}
}

class MachineRunnable implements Runnable
{
	public MachineRunnable(EMachine m)
	{
		machine = m;
	}
	
	public void run()
	{
		machine.Continue();
	}
	private EMachine machine;
}





<<<<<<< .mine



=======




>>>>>>> .theirs