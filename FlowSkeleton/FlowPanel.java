package FlowSkeleton;
import java.util.List;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CyclicBarrier;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FlowPanel extends JPanel implements Runnable {
	Terrain land;

	int id;
	volatile int timestep;
	public volatile boolean pause;
	public volatile boolean suspend;

	CyclicBarrier barrier;


	FlowPanel(Terrain terrain) {
		land=terrain;
		pause = true;
		suspend = false;
		this.barrier = new CyclicBarrier(4);;
	}
		
	// responsible for painting the terrain and water
	// as images
	@Override
    protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		
		super.paintComponent(g);
		
		// draw the landscape in greyscale as an image



		if (land.getImage() != null){
			g.drawImage(land.getImage(), 0, 0, null);
			g.drawImage(land.water.getImage(), 0, 0, null);
			
		}
		
	}
	
	// main thread run method
	 public void run() {	
		 while(!suspend){

		if(!pause){

		// get thread ID
		String str = Thread.currentThread().getName();
		id = Integer.parseInt(str);

		// Calculate permute size
		int length = land.permute.size();
		int poolSize = length / 4;

		// Calculate start and stop index
		int startIndex = id * poolSize;
		int stopIndex = startIndex + poolSize;

		if(id == 3){
			stopIndex = length;
		}
		// clear specific water boundary

		// divide permute list into work
		List<Integer> work = land.permute.subList(startIndex, stopIndex);
		// simulation step 
		land.water.step(work);

		try{
		// wait for other threads to finish
		barrier.await();
		
		}catch(Exception e){
			e.printStackTrace();
		}

		// display loop here
		// to do: this should be controlled by the GUI
		// to allow stopping and starting
		
	}

	// Draw water
	land.water.drawWater();
	repaint();

	}
	}
}