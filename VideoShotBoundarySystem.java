/* 
  Tony Le Ass 4
*/

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static javax.swing.UIManager.getColor;
import java.lang.Object;
import java.math.RoundingMode;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import javax.swing.*;
import java.util.Random; 


public class VideoShotBoundarySystem extends JFrame {

	private JLabel photographLabel = new JLabel(); // container to hold a large
	private JButton[] button; // creates an array of JButtons
	private int[] buttonOrder = new int[101]; // creates an array to keep up with the image order
	private int[] cut = new int[101]; 
	private int[] trans = new int[101]; 
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private GridLayout gridLayout4;
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelTop;
	private JPanel buttonPanel;
	private Double[][] intensityMatrix = new Double[5000][26];
	 
	int picNo = 0;
	int imageCount = 1; // keeps up with the number of images displayed since the first page.
	int pageNo = 1;

	private double[] sD = new double[5000]; 
	double averageSD = 0;

	// Thresholds
	double tB; 
	double tS; 
	int tor;

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				VideoShotBoundarySystem app = new VideoShotBoundarySystem();
				app.setVisible(true);
			}
		});
	}

	public VideoShotBoundarySystem() {
		// The following lines set up the interface including the layout of the buttons and JPanels.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		setTitle("Video Shot Boundary Detection");
		panelBottom1 = new JPanel();
		panelBottom2 = new JPanel();
		panelTop = new JPanel();
		buttonPanel = new JPanel();
		gridLayout1 = new GridLayout(4, 5, 5, 5);
		gridLayout2 = new GridLayout(2, 1, 5, 5);
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		gridLayout4 = new GridLayout(2, 2, 2, 2);
		setLayout(gridLayout2);
		panelBottom1.setLayout(gridLayout1);
		panelBottom2.setLayout(gridLayout1);
		panelTop.setLayout(gridLayout3);
		add(panelTop);
		add(panelBottom1);
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(gridLayout4);
		panelTop.add(photographLabel);

		panelTop.add(buttonPanel);
		JButton previousPage = new JButton("Previous Page");
		JButton nextPage = new JButton("Next Page");
		JButton displayAllFrames = new JButton("All Frames");
		JButton randomCut = new JButton("Random Cut"); 
		buttonPanel.add(previousPage);
		buttonPanel.add(nextPage);
		buttonPanel.add(displayAllFrames);
		buttonPanel.add(randomCut); 
	
		nextPage.addActionListener(new nextPageHandler());
		previousPage.addActionListener(new previousPageHandler());
		displayAllFrames.addActionListener(new displayAllFramesHandler());
		randomCut.addActionListener(new randomCutHandler()); 

		setSize(1100, 750);
		// this centers the frame on the screen
		setLocationRelativeTo(null);

		button = new JButton[101];

		for(int i = 1; i < 81; i++){
			button[i] = new JButton("" + i);
			button[i].addActionListener(new IconButtonHandler(i));
		} 
		readIntensityFile();
		calculateSDHelper();
		setThresholds();
		calculateCutsAndGradTransitions();
		displayFirstPage();
	}
	
	/*
	 * This method opens the intensity text file containing the intensity matrix
	 * with the histogram bin values for each image.
	 * The contents of the matrix are processed and stored in a two dimensional
	 * array called intensityMatrix.
	 */
	public void readIntensityFile() {
		Scanner read; 
		double intensityBin;
		try {
			read = new Scanner(new File("intensity.txt"));
			for (int i = 1000; i < 5000; i++) {
				for (int j = 1; j < 26; j++) {
					if (read.hasNext()) { 
						intensityBin = read.nextDouble();
						intensityMatrix[i][j] = intensityBin; 
					}
				}
			}
		} catch (FileNotFoundException EE) {
			// if the txt file isnt found then throw an exception
			System.out.println("The file intensity.txt does not exist");
		}
	} 

	/*
	 * This method displays the first twenty images in the panelBottom. The for loop
	 * starts at number one and gets the image
	 * number stored in the buttonOrder array and assigns the value to imageButNo.
	 * The button associated with the image is
	 * then added to panelBottom1. The for loop continues this process until twenty
	 * images are displayed in the panelBottom1
	 */
	private void displayFirstPage() {
		imageCount = 1;
		panelBottom1.removeAll();
		for (int i = 1; i < 21; i++) {
			panelBottom1.add(button[i]); 
			imageCount++;
		}
		panelBottom1.revalidate();
		panelBottom1.repaint();
	}

	/*
	 * This class implements an ActionListener for each iconButton. When an icon
	 * button is clicked, the image on the
	 * the button is added to the photographLabel and the picNo is set to the image
	 * number selected and being displayed.
	 */
	private class IconButtonHandler implements ActionListener {
		int pNo = 0; 
		ImageIcon iconUsed; 
		IconButtonHandler(int i) { 
			pNo = i; 
		}
		public void actionPerformed(ActionEvent e) {	
			System.out.println(buttonOrder[pNo]);
			iconUsed = new ImageIcon("videoFrames/frame" + buttonOrder[pNo] + ".jpg");
			photographLabel.setIcon(iconUsed); 
		}
	}
	
	/*
	 * This class implements an ActionListener for the nextPageButton. The last
	 * image number to be displayed is set to the
	 * current image count plus 20. If the endImage number equals 101, then the next
	 * page button does not display any new
	 * images because there are only 100 images to be displayed. The first picture
	 * on the next page is the image located in
	 * the buttonOrder array at the imageCount
	 */
	private class nextPageHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int endImage = imageCount + 20;
			if (endImage <= 101) {
				panelBottom1.removeAll();
				for (int i = imageCount; i < endImage; i++) {
					panelBottom1.add(button[i]);
					imageCount++;
				}
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}

	}

	/*
	 * This class implements an ActionListener for the previousPageButton. The last
	 * image number to be displayed is set to the
	 * current image count minus 40. If the endImage number is less than 1, then the
	 * previous page button does not display any new
	 * images because the starting image is 1. The first picture on the next page is
	 * the image located in
	 * the buttonOrder array at the imageCount
	 */
	private class previousPageHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;
			if (startImage >= 1) {
				panelBottom1.removeAll();
				/*
				 * The for loop goes through the buttonOrder array starting with the startImage
				 * value
				 * and retrieves the image at that place and then adds the button to the
				 * panelBottom1.
				 */
				for (int i = startImage; i < endImage; i++) { 
					panelBottom1.add(button[i]);
					imageCount--;
				}
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}
	}

	/*
	* To output all cuts and transitions to console 
	*/
	private class displayAllFramesHandler implements ActionListener {
		public void actionPerformed(ActionEvent e)  {
			for (int i = 0; i < 101; i++) {
				if (cut[i] != 0) {
					System.out.println("Cut at: " + cut[i]);
				}
				if (trans[i] != 0) {
					System.out.println("Transition at: " + trans[i]);
				}
			} 
		}
	}

	// RandomCutHandler just for randomButton, it'll display a random cut and output to console and display frame 
	private class randomCutHandler implements ActionListener { 
		public void actionPerformed(ActionEvent e) { 
			ImageIcon iconUsed; 
			Random ran = new Random(); 
			int low = 1; 
			int high = 28; 
			int result = ran.nextInt(high - low) + low; 
			while(button[result] == null) { 
				result = ran.nextInt(high - low) + low; 
			}
			System.out.println(buttonOrder[result]);
			iconUsed = new ImageIcon("videoFrames/frame" + buttonOrder[result] + ".jpg");
			photographLabel.setIcon(iconUsed); 
		
		}
	}

	// formula - https://gyazo.com/837ee0f750c2d9b17026234c031d96a8
	// Helper Function for SD, will also get the average. 
	public void calculateSDHelper() {
		double sum = 0; 
		for(int i = 1000; i < 4999; i++) { // loop through til 4999, do not need to go to 5k bc of + 1
			double d = 0;
			for(int j = 1; j < 26; j++) {  // loop through each bin 
				d += Math.abs(intensityMatrix[i][j] - intensityMatrix[i+1][j]); // get distancce 
			}
			sD[i] = d;	
		}	
		for (int i = 1000; i < 4999; i++) {
			sum += sD[i]; 
		}
		averageSD = sum / 3999; 
	}

	// set thresholds 
	// calcualtes average and stdev for the tB and tS 
	public void setThresholds(){
		double avg = 0;
		double stdev = 0; 
		for(int i = 1000; i < 5000; i++) { // loops through each and adds for avg and calculates stdev without the sqrt yet 
			avg += sD[i];
			stdev += Math.pow((sD[i] - averageSD), 2);
		}
		avg /= 3999;
		stdev = Math.sqrt(stdev/3999); 
		tB = avg + (stdev * 11); // for cut 
		tS = avg * 2; // for gradual transition 
		tor = 2;
		System.out.println(tB + " " + tS);
	}

	// calculates the cuts and transitions 
	public void calculateCutsAndGradTransitions() {
		int count = 1; 
		for (int i = 1000; i < 5000; i++) { 
			if (sD[i] >= tB) { // if is above tB then its a cut 
				cut[count] = i - 1; 
				buttonOrder[count] = i - 1; 
				count++;
			}
			if (tS <= sD[i] && sD[i] < tB && i < 4997) { // tS <= sD < tB 
				int fsCandi = i; //potential start
				int feCandi = i; //potential end 
				double grad = 0; //sum or total 
				boolean bool = true; 
				while (bool) {
					if(sD[feCandi + 1] >= tB) { // if next frame is greater or equal to tB then its a cut 
						cut[count] = feCandi; 
						buttonOrder[count] = feCandi; 
						count++; // increment count	
						i++;  // can skip sinc ealrdy checked
						bool = false; // break out 
					}
					if (sD[feCandi + 1] < tS && sD[feCandi + 2] < tS) { // checking for next frame and the frame after that for both less than tS 
						for(int j = fsCandi; j <= feCandi; j++) { // loop to add from start to end 
							grad += sD[j];  
						}
						if(grad >= tB) { // if total is greater than tB then its a transition
							trans[count] = fsCandi - 1; 
							buttonOrder[count] = fsCandi - 1;
							count++;
						}
						bool = false;
					}
					feCandi++;
					i++;
				}
			}
		}	
	}
}
 