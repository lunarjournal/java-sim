package FlowSkeleton;

import javax.swing.*;
import java.awt.*;  
import java.awt.event.*;  
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.util.concurrent.CyclicBarrier;

public class Flow {
	static long startTime = 0;
	static int frameX;
	static int frameY;
	static FlowPanel fp;

	// start timer
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	
	// stop timer, return time elapsed in seconds
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}
	
	// setup GUI
	public static void setupGUI(int frameX,int frameY, Terrain landdata) {
		
		Dimension fsize = new Dimension(800, 800);
    	JFrame frame = new JFrame("Waterflow"); 
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().setLayout(new BorderLayout());
    	
      	JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS)); 
		fp = new FlowPanel(landdata);
	
		fp.setPreferredSize(new Dimension(frameX,frameY));
		g.add(fp);
	    
	   	
		JPanel b = new JPanel();
		b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));
		
		// add GUI elements
		JButton resetB = new JButton("Reset");
		JButton pauseB = new JButton("Pause");
		JButton playB = new JButton("Play");
		JButton endB = new JButton("End");
		JLabel step = new JLabel("Timestep: 0");

	
		// add action listeners
		// end button
		endB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				fp.suspend = true;
				frame.dispose();
			}
		});
		// reset button
		resetB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				fp.land.water.clearWater();
			}
		});

		// pause button
		pauseB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){fp.pause = true;}
		});

		// play button
		playB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){fp.pause = false;}
		});
	

		// add mouse listener
		g.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();

				fp.land.water.addPatch(e.getX(), e.getY(), 8);
			}

			public void mouseEntered(MouseEvent e) {}  
			public void mouseExited(MouseEvent e) {}  
			public void mousePressed(MouseEvent e) {}  
			public void mouseReleased(MouseEvent e) {}  
		});
		
		// add GUI elements to b
		b.add(step);
		b.add(Box.createVerticalStrut(20));
		b.add(resetB);
		b.add(pauseB);
		b.add(playB);
		b.add(endB);
		g.add(b);
    	
		frame.setSize(frameX, frameY+50);	// a little extra space at the bottom for buttons
      	frame.setLocationRelativeTo(null);  // center window on screen
      	frame.add(g); //add contents to window
        frame.setContentPane(g);
		frame.setVisible(true);
	
		// create 4 worker threads
		for(int i = 0; i < 4; i++){
			Thread fpt = new Thread(fp, Integer.toString(i));
			// start thread
			fpt.start();
		}
	}
	
	public static void main(String[] args) {
		Terrain landdata = new Terrain();
		
		// check that number of command line arguments is correct
		if(args.length != 1)
		{
			System.out.println("Incorrect number of command line arguments. Should have form: java -jar flow.java intputfilename");
			System.exit(0);
		}
				
		// landscape information from file supplied as argument
		// 
		landdata.readData(args[0]);
		
		frameX = landdata.getDimX();
		frameY = landdata.getDimY();
		SwingUtilities.invokeLater(()->setupGUI(frameX, frameY, landdata));
		
	}
}
