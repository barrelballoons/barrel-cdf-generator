/*
FrameHolder.java

Description:
   Stores the data frames that are being processed

   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   This file is part of The BARREL CDF Generator.

   The BARREL CDF Generator is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   The BARREL CDF Generator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along with 
   The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*/

package edu.ucsc.barrel.cdf_gen;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FrameHolder{
   private String payload;
   private short dpuId;
   private int 
      first_fc = 0,
      last_fc = 0;
   private Map<Integer, BarrelFrame> frames;
   private List<Integer> ordered_fc;
   
   //variables to keep track of valid altitude range
   private float min_alt;
   private boolean low_alt = true;
   
   //variables to  signal frame counter rollover
   private boolean fc_rollover = false;
   private Integer last_fc = 0;

   public FrameHolder(final String p, final short id){
      this.frames = new HashMap<Integer, BarrelFrame>();
      this.ordered_fc = new LinkedList<Integer>();

      this.payload = (p.split(","))[0];
      this.dpuId = id;
      
      //set minimum altitude based on either command line argument or
      //default setting in the Constants class
      if(CDF_Gen.getSetting("min_alt").equals("")){
         min_alt = Constants.ALT_MIN;
      }else{
         min_alt = Float.parseFloat(CDF_Gen.getSetting("min_alt"));
      }
      System.out.println("Rejecting data bellow " + min_alt + " kilometers.");
      
      //Figure out if the previous CDF file had a frame counter rollover
      if(new File("fc_rollovers/" + payload).exists()){
        this.fc_rollover = true; 
      }
   }

   public void addFrame(BigInteger rawFrame){
      BarrelFrame frame = new BarrelFrame(rawFrame, this.dpuId);
      int fc = frame.getFrameCounter();

      //check for fc rollover
      if(this.fc_rollover){
         fc += Constants.FC_OFFSET;
         frame.setFrameCounter(fc);
      } else {
         if ((this.last_fc - fc) > Constants.LAST_DAY_FC) {
            //rollover detected
            this.fc_rollover = true;
            
            fc += Constants.FC_OFFSET;

            CDF_Gen.log.writeln(
               "Payload " + payload + " rolled over after fc = " + this.last_fc 
            );

            //offset fc
            frame.setFrameCounter(fc);

            //create an empty file to indicate rollover
            (new Logger("fc_rollovers/" + payload)).close();
         }
      }
      
      //update the first and last frame numbers
      this.first_fc = Math.min(this.first_fc, fc);
      this.last_fc = Math.max(this.last_fc, fc);

      //add the frame to the map
      this.frames.put(fc, frame);
      this.ordered_fc.add(fc);
   }
   
   public int[] getDateFrames(int date){
      int[] range = new int[2];
      return range;
   }
   
   public int[] getFcRange(){
      return new int[](this.first_fc, this.last_fc);
   }

   public BarrelFrame[] getFrames(int start, int stop){
      BarrelFrame[] 
         results,
         frames = BarrelFrame[stop - start];
      int 
         fc,
         frame_i = 0;
      
      for (fc = start; fc <= stop; fc++) {
         if(this.frames.containsKey(fc)){
            frames[frame_i] = this.frames.get(fc);
            frame_i++;
         }
      }

      System.arraycopy(frames, 0, results, 0, frame_i);

      return results;
   }
   public BarrelFrame[] getFrames(int[] range){
      if(range.length != 2){
         CDF_Gen.out.log(2, "Invalid frame range: " + range.join(", "));
      }
      return this.getFrames.get(range[0], range[1]);
   }

   public int size(){
      return this.frames.size();
   }

   public BarrelFrame fcIterator(){
      return this.ordered_fc.iterator();
   }

/*
   public Map<String, Number> getData(String type, int start, int stop){
      Map<Integer, Short> data = new Map<Integer, Short>();
      BarrelFrame frame;

      //copy the frame data into an array that is sized for no data gaps
      for (int fc = start; fc <= stop; fc++) {
         if ((frame = this.getFrame(fc)) != null) {
            //only touch the array if there is a valid frame
            data.put(fc, frame.getPPS());
            frame_i++;
         }
      }

      //trim excess elements left by data gaps
      System.arraycopy(data, 0, result, 0, frame_i);

      return data;
   }
   
   public Map<Integer, Short> getPPS(int start, int stop){
      Map<Integer, Short> data = new Map<Integer, Short>();
      BarrelFrame frame;

      //copy the frame data into an array that is sized for no data gaps
      for(int fc = start; fc <= stop; fc++){
         if ((frame = this.getFrame(fc)) != null) {
            //only touch the array if there is a valid frame
            data.put(fc, frame.getPPS());
            frame_i++;
         }
      }

      //trim excess elements left by data gaps
      System.arraycopy(data, 0, result, 0, frame_i);

      return data;
   }

   public Map<Integer, Short> getPayID(int start, int stop){
      Short[] data = new Short[start - stop], result;
      BarrelFrame frame;
      int fc, frame_i;

      //copy the frame data into an array that is sized for no data gaps
      for(fc = start; fc <= stop; fc++){
         if ((frame = this.getFrame(fc)) != null) {
            //only touch the array if there is a valid datum
            data[frame_i] = frame.getPayloadID();
            frame_i++;
         }
      }

      //trim excess elements left by data gaps
      System.arraycopy(data, 0, result, 0, frame_i);

      return result;
   }

   public short getDPUVersion(){
      return this.version;
   }

   public Map<Integer, Short> getDPUVersion(int start, int stop){
      Short[] data = new Short[start - stop], result;
      BarrelFrame frame;
      int fc, frame_i;

      //copy the frame data into an array that is sized for no data gaps
      for(fc = start; fc <= stop; fc++){
         if ((frame = this.getFrame(fc)) != null) {
            //only touch the array if there is a valid datum
            data[frame_i] = frame.getPayloadID();
            frame_i++;
         }
      }

      //trim excess elements left by data gaps
      System.arraycopy(data, 0, result, 0, frame_i);

      return result;
   }

   public Map<Integer, Short> getNumSats(int start, int stop){
      
   }

   public Map<Integer, Short> getOffset(int start, int stop){
      
   }

   public Map<Integer, Short> getTermStatus(int start, int stop){
      
   }

   public Map<Integer, Short> getModemCounter(int start, int stop){
      
   }

   public Map<Integer, Short> getDcdCounter(int start, int stop){
      
   }

   public Map<Integer, Integer> getWeek(int start, int stop){
      
   }

   public Map<Integer, Integer> getCmdCounter(int start, int stop){
      
   }

   public Map<Integer, Integer[]> getGPS(int start, int stop){
      
   }
   
   public Map<Integer, Integer[]> getMAG(int start, int stop){
      
   }

   public Map<Integer, Integer[]> getMSPC(int start, int stop){
      
   }

   public Map<Integer, Integer[]> getSSPC(int start, int stop){
      
   }
   
   public Map<Integer, Map<Integer, Integer[]>> getFSPC(int start, int stop){
      
   }
   
   public Map<Integer, Long> getEpoch(int start, int stop){
      
   }

   public Map<Integer, Long[]> getHousekeeping(int start, int stop){
      
   }

   public Map<Integer, Long[]> getRateCounters(int start, int stop){
      
   }   
   */
}
