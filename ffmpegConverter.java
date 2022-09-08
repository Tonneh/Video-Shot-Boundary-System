/* 
  Tony Le Ass 4
*/

import java.io.IOException;

public class ffmpegConverter {
    public static void main(String[] args) { 
        try {
            Process p = Runtime.getRuntime().exec("ffmpeg -i 20020924_juve_dk_02a.avi videoFrames/frame%d.jpg"); 
        } catch (IOException e) {
            System.out.println("ffmpeg fail"); 
        }
    }
}
