package FlowSkeleton;
import java.util.List;

import java.awt.*; 
import java.util.Collections;
import java.util.ArrayList;
import java.awt.image.*;

public class Water {

    // class definitions
    volatile int [][] depth; // water depth values
    volatile float [][] surface; // water surface values
    volatile float [][] height; // water height values
    int dimx, dimy; // x and y grid lengths
    volatile BufferedImage img; // water pixel overlay

    // general comparison class
    public class Compare{
        Float value;
        int x;
        int y;

        Compare(Float value, int x, int y){
            this.value = value;
            this.x = x;
            this.y = y;
        }
    };

    // get image buffer
	public BufferedImage getImage() {
        return img;
    }

    // initialize class definitions
     void initialize(float [][] height){
        this.dimx = height[0].length;
        this.dimy = height.length;
        this.depth = new int[dimx][dimy];
        this.surface = new float[dimx][dimy];
        this.height = height;
        img = new BufferedImage(this.dimx, this.dimy, BufferedImage.TYPE_INT_ARGB);
    }

    // clear water surface boundary
    synchronized void clearBoundary(){
             // x=0
            for(int i = 0; i < dimy; i++){
                depth[i][0] = 0;
                depth[i][dimx-1] = 0;
            }
            // y=0
            for(int i = 0; i < dimx; i++){
                depth[0][i] = 0;
                depth[dimy-1][i] = 0;
            }
        
    }

    // check if x,y falls within boundary
    boolean checkBoundary(int x, int y){
        return (x!=0 && y!=0 && x!=dimx-1 && y!=dimy-1);
    }

    // add patch of water of size n to x,y
    synchronized void addPatch(int x, int y, int n){
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n;j++){
                if((x+j) <= dimx && (y+i) <= dimy){
                depth[y + i][x + j] = 10;
                }
            }
        }
    }
    // clear water from grid
    synchronized void clearWater(){
        for(int i = 0; i < dimx * dimy; i++){
            
            int index = i;
            int x = index % dimx;
            int y = index / dimx;

            depth[y][x] = 0;
        }
    }
    // draw water pixels
    synchronized void drawWater(){

        for(int i = 0; i < dimx * dimy; i++){
            
            int index = i;
            int x = index % dimx;
            int y = index / dimx;
          
            Color blue = new Color(0,0,255,255);
            Color empty = new Color(0,0,0,0);

            if(depth[y][x] > 0){
                img.setRGB(x, y, blue.getRGB());
            }else{
                img.setRGB(x, y, empty.getRGB());
            }

        }

    }
     // simulation step
     synchronized void step(List<Integer> indices){

        List<Compare> compare = new ArrayList<>(); 
        int index =0;
        int x = 0;
        int y = 0;
    
        // clear water surface boundary
        clearBoundary();

        // water surface pass
        for(int i = 0; i < indices.size(); i++){
            index = indices.get(i);
            x = index % dimx;
            y = index / dimx;

            if(checkBoundary(x, y)){
            surface[y][x] = (0.01f * depth[y][x]) + height[y][x];
            }

        }
   
        // depth pass
        for(int i = 0; i < indices.size(); i++){
            index = indices.get(i);
            x = index % dimx;
            y = index / dimx;

            if(checkBoundary(x, y) && depth[y][x] > 0){
            float base = surface[y][x];

            compare.clear();
            compare.add(new Compare(surface[y-1][x], x, y-1)); // top
            compare.add(new Compare(surface[y+1][x], x, y+1)); // bottom
            compare.add(new Compare(surface[y][x-1], x-1, y)); // left
            compare.add(new Compare(surface[y][x+1], x+1, y)); // right
            compare.add(new Compare(surface[y-1][x-1], x-1, y-1)); // top diag left
            compare.add(new Compare(surface[y-1][x+1], x+1, y-1)); // top diag right
            compare.add(new Compare(surface[y+1][x-1], x-1, y+1)); // bottom diag left
            compare.add(new Compare(surface[y+1][x+1], x+1, y+1)); // bottom diag right
            
            float min = 999999.0f;
            int min_index = 0xff;

            // find lowest neighbour
            for(int z = 0; z < compare.size(); z++){
                Compare comp = compare.get(z);
                if(comp.value < min){
                    min = comp.value;
                    min_index = z;
                } 
   
            }

            if(min < base){       
            if(depth[y][x]!=0)
             depth[y][x] -= 1;
         
            Compare comp = compare.get(min_index);
            x = comp.x;
            y = comp.y;
      
            depth[y][x] +=1;

            }
        }

        }

    }

}