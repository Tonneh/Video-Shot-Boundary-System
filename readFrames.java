/*
Tony Le Ass 4 
*/

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class readFrames {
	  int imageCount;
	  double intensityMatrix [][] = new double[5000][26];  
	  double intensityBin [] = new double[26]; 

	  public readFrames() {  
		imageCount = 1000; 
		while (imageCount < 5000) {
			try { 
				//reading in frames, using ImageIO 
				BufferedImage image = ImageIO.read(new File("videoFrames/frame" + imageCount + ".jpg"));
				// initializing the intensityBins 
				for (int i = 1; i < 26; i++) {
					intensityBin[i] = 0; 
				}
				// getting width and height of image 
				int width = image.getWidth(); 
				int height = image.getHeight(); 
				// get intensity 
				getIntensity(image, height, width);
				//increment count
				imageCount++; 
			} catch (IOException e) { 
				System.out.println("Error occurred when reading the file.");
			}
		}
	    writeIntensity(); // writes the intensity file
	  }
	  
	  // intensity method 
	  public void getIntensity(BufferedImage image, int height, int width){
		  // i represents rows
		  for (int i = 0; i < height; i++) {
			  // j represents the column 
			  for (int j = 0; j < width; j++) {
				  // Color object c gets image's rgb
				  Color c = new Color (image.getRGB(j, i)); 
				  int red = c.getRed(); 
				  int green = c.getGreen(); 
				  int blue = c.getBlue(); 
				  // (I = 0.299R + 0.587G + 0.114B) 
				  double intensity = (0.299 * red + 0.587 * green + 0.144 * blue); 
				  // Histogram bins from 0-240 is increments by 1 every 10, and 240-255 increments 
				  // 1 for 15. 
				  if (intensity >= 0 && intensity < 240) {
					  intensityBin[(int) intensity / 10 + 1]++; 
				  } else if (intensity >= 240 && intensity < 255){
					  intensityBin[25]++; 
				  }
			  }
		  }
		   //Puts the intensityBins into intensityMatrix 2d array 
		   for (int i = 1; i < 26; i++) {
			intensityMatrix[imageCount][i] = intensityBin[i];
		}
	  }
	  
	  
  // This method writes the contents of the intensity matrix to a file called
  // intensity.txt
  public void writeIntensity() {
    try {
      FileWriter fstream = new FileWriter("intensity.txt", true);
      BufferedWriter out = new BufferedWriter(fstream);
      for (int i = 1000; i < 5000; i++) {
        for (int j = 1; j < 26; j++) {
          out.write(intensityMatrix[i][j] + " ");
        }
        out.newLine();
      }
      out.close();
    } catch (IOException e) {
      System.out.print("Error occurred when creating the file.");
    }
  }
	
	public static void main(String[] args) {
		new readFrames();
	}
	
}
