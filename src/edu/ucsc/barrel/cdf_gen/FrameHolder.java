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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import gsfc.nssdc.cdf.util.CDFTT2000;

public class FrameHolder{
   private String payload;
   private int
      dpuId, dpuVer;
   private List<Integer> ordered_fc;
   private Map<Integer, BarrelFrame> frames;
   private Map<Integer, Boolean> low_alt_frames;
   private Map<String, Integer> numRecords;
   private Map<String, Long> currentFrameGroup;
    
   //variables to keep track of valid altitude range
   private float min_alt;
   private boolean low_alt = true;
   
   //variables to  signal frame counter rollover
   private boolean fc_rollover = false;
   private Integer
      first_fc = 0,
      last_fc = 0;

   public FrameHolder(final String p, final int id, float alt){
      this.ordered_fc     = new LinkedList<Integer>();
      this.frames         = new HashMap<Integer, BarrelFrame>();
      this.low_alt_frames = new HashMap<Integer, Boolean>();

      //Start all of the record counters at 0
      this.numRecords = new HashMap<String, Integer>(6);
      this.numRecords.put("20Hz",  0);
      this.numRecords.put("4Hz",   0);
      this.numRecords.put("1Hz",   0);
      this.numRecords.put("mod4",  0);
      this.numRecords.put("mod32", 0);
      this.numRecords.put("mod40", 0);

      //Start all of the frameGroups at -1 
      this.currentFrameGroup = new HashMap<String, Long>(6);
      this.currentFrameGroup.put("20Hz",  -1L);
      this.currentFrameGroup.put("4Hz",   -1L);
      this.currentFrameGroup.put("1Hz",   -1L);
      this.currentFrameGroup.put("mod4",  -1L);
      this.currentFrameGroup.put("mod32", -1L);
      this.currentFrameGroup.put("mod40", -1L);

      this.payload = (p.split(","))[0];
      this.dpuId = id;
      this.min_alt = alt * 1000000; //convert km to mm
      
      System.out.println("Rejecting data bellow " + min_alt + " kilometers.");
      
      //Figure out if the previous CDF file had a frame counter rollover
      if(new File("fc_rollovers/" + payload).exists()){
        this.fc_rollover = true;
      }
   }

   public void addFrame(BigInteger rawFrame){
      BarrelFrame frame = new BarrelFrame(rawFrame, this.dpuId);
      long
         fc = frame.getFrameCounter(),
         mod4fg = fc - frame.mod4,
         fg;
      
      //update the dpu version number
      this.dpuVer = frame.getDPUVersion();

      //skip this frame if its group has been tagged as low altitude
      if(this.low_alt_frames.containsKey((int)mod4fg)){
         return;
      }

      //check if this frame has an altitude attached to it
      if(frame.mod4 == Ephm.ALT_I && frame.getGPS() < this.min_alt){
         //save the framge group
         this.low_alt_frames.put((int)mod4fg, true);

         //delete any frames that have already been saved
         this.frames.remove((int)(mod4fg + Ephm.TIME_I));
         this.frames.remove((int)(mod4fg + Ephm.LAT_I));
         this.frames.remove((int)(mod4fg + Ephm.LON_I));

         //blacklist this framegroup
         this.low_alt_frames.put((int)mod4fg, true);

         //skip to the next frame
         return;
      }

      //check for fc rollover
      if(this.fc_rollover){
         fc += BarrelFrame.FC_OFFSET;
         frame.setFrameCounter((int)fc);
      } else {
         if ((this.last_fc - fc) > BarrelFrame.LAST_DAY_FC) {
            //rollover detected
            this.fc_rollover = true;
            
            fc += BarrelFrame.FC_OFFSET;

            CDF_Gen.log.writeln(
               "Payload " + payload + " rolled over after fc = " + this.last_fc 
            );

            //offset fc
            frame.setFrameCounter((int)fc);

            //create an empty file to indicate rollover
            (new Logger("fc_rollovers/" + payload)).close();
         }
      }
      
      //update the first and last frame numbers
      this.first_fc = (int) Math.min(this.first_fc, fc);
      this.last_fc = (int)Math.max(this.last_fc, fc);

      //add the frame to the map
      this.frames.put((int)fc, frame);
      this.ordered_fc.add((int)fc);

      //update the record counters and framegroups for 1Hz or faster
      this.currentFrameGroup.put("20Hz", fc);
      this.currentFrameGroup.put("4Hz", fc);
      this.currentFrameGroup.put("1Hz", fc);
      this.numRecords.put("20Hz", this.numRecords.get("20Hz") + 20);
      this.numRecords.put("4Hz", this.numRecords.get("4Hz") + 4);
      this.numRecords.put("1Hz", this.numRecords.get("1Hz") + 1);

      //check to see if the frame group has changed before updating for <1Hz
      fg = fc - frame.mod4;
      if (fg != this.currentFrameGroup.get("mod4")) {
         this.currentFrameGroup.put("mod4", fg);
         this.numRecords.put("mod4", this.numRecords.get("mod4") + 1);
      }
      fg = fc - frame.mod32;
      if (fg != this.currentFrameGroup.get("mod32")) {
         this.currentFrameGroup.put("mod32", fg);
         this.numRecords.put("mod32", this.numRecords.get("mod32") + 1);
      }
      fg = fc - frame.mod40;
      if (fg != this.currentFrameGroup.get("mod40")) {
         this.currentFrameGroup.put("mod40", fg);
         this.numRecords.put("mod40", this.numRecords.get("mod40") + 1);
      }
   }

   public int getDpuVersion() {
      return this.dpuVer;
   }

   public BarrelFrame getFrame(int fc) {
      return this.frames.get(fc);
   }
   
   public int[] getDateFrames(int date){
      int[] range = new int[2];
      return range;
   }
   
   public int[] getFcRange(){
      int[] range = {this.first_fc, this.last_fc};
      return range;
   }

   public BarrelFrame[] getFrames(int start, int stop){
      BarrelFrame[]
         results,
         frames = new BarrelFrame[stop - start];
      int 
         fc,
         frame_i = 0;
      
      for (fc = start; fc <= stop; fc++) {
         if(this.frames.containsKey(fc)){
            frames[frame_i] = this.frames.get(fc);
            frame_i++;
         }
      }
      results = new BarrelFrame[frame_i];
      System.arraycopy(frames, 0, results, 0, frame_i);

      return results;
   }
   public BarrelFrame[] getFrames(int[] range){
      if(range.length != 2){
         CDF_Gen.log.writeln("Invalid frame range: " + Arrays.toString(range));
      }
      return this.getFrames(range[0], range[1]);
   }

   public BarrelFrame[] getFrames() {
      return this.getFrames(this.first_fc, this.last_fc);
   }

   public Integer getNumRecords(String cadence) {
      return this.numRecords.get(cadence);
   }

   public Integer getNumFrames() {
      return this.getNumRecords("1Hz");
   }

   public Integer size(){
      return this.getNumRecords("1Hz");
   }

   public Iterator<Integer> fcIterator(){
      return this.ordered_fc.iterator();
   }

   public List<Integer> getFcByDate(int date){
      long
         rec_date = 0;
      long[]
         tt2000_parts; 
      Integer
         fc;
      BarrelFrame
         frame;
      List<Integer>
         fcs = new ArrayList<Integer>();
      Iterator<Integer>
         fc_i = this.fcIterator();

      //find the first and last frame coutner values for this day
      while (fc_i.hasNext()){
         fc = fc_i.next();
         frame = CDF_Gen.frames.getFrame(fc);
         tt2000_parts = CDFTT2000.breakdown(CDF_Gen.barrel_time.getEpoch(fc));
         rec_date = 
            tt2000_parts[2] + //day
            (100 * tt2000_parts[1]) + //month
            (10000 * (tt2000_parts[0] - 2000)); //year
         if(rec_date == date){
            //found the first_fc index
            fcs.add(fc);
         }
      }

      return fcs;
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
