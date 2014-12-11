/*
ExactTiming.java

Description:
   Uses a block of gps time info to create a more exact time variable.
   Ported from MPM's C code.

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

import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class ExtractTiming {
   //Set some constant values
   private static final byte
      MINPPS    = 0,
      MINMS     = 1,
      MINFC     = 0;
   private static final short
      MINWK     = 1200,
      MAXWK     = 1880,
      MAXPPS    = 1000;
   private static final int
      MAX_RECS  = 500,// max number of mod4 recs for model
      MAXMS     = 691200000,
      MAXFC     = 2097152;
   private static final long
      MSPERWEEK = 604800000L,// mseconds in a week
      GPS_EPOCH = -630763148816L;//ms from Jan 6, 1980 to J2000
   private static final double
      NOM_RATE  = 999.89;// nominal ms per frame
   
   //offsets for spectral data. These offsets will move the epoch variable so
   //it points to the middle of the accumulation time
   private static final long 
      SSPC_EPOCH_OFFSET = 16000000000L, //31968000000L;
      MSPC_EPOCH_OFFSET = 2000000000L; //3996000000L;

   private BarrelFrame[] frames;
   private int numFrames, numRecords;
   private int[] fcRange;

   private class TimeRec{
      private long
         ms_of_week,
         weeks_in_ms,
         ms;//frame timestamp
      private int 
         frame,//frame counter
         week, pps;
      private short
         extra_ms;

      public TimeRec(int fc, long msw, int w, int p){
         this.week        = w;
         this.pps         = p;
         this.frame       = fc;
         this.ms_of_week  = msw;
         this.weeks_in_ms = week * MSPERWEEK;

         //figure out if we need to add an extra second based on the PPS
         this.extra_ms = (short)(pps < 241 ? 0 : 1000);

         //calculate the number of milliseconds since J2000 
         this.ms = calcEpoch();
      }
      
      public long  getMS()   {return ms;}
      public long  getMSW()  {return ms_of_week;}
      public int   getFrame(){return frame;}
      public int   getWeek() {return week;}
      public int   getPPS()  {return pps;}

      public void setFrame(int fc){
         this.frame = fc;
      }
      public void setMSW(long msw){
         this.ms_of_week = msw;
         this.ms = calcEpoch();
      }
      public void setPPS(int p){
         this.pps = p;
         this.ms = calcEpoch();
      }
      public void setWeek(int w)  {
         this.week = w;
         this.weeks_in_ms = week * MSPERWEEK;
         this.ms = calcEpoch();
      }
      
      private long calcEpoch() {
         return
            this.weeks_in_ms +this.ms_of_week +extra_ms -this.pps +GPS_EPOCH;
      }
   }
   
   private class LinModel{
      private long first_frame, last_frame;
      private double slope, intercept;

      public void   setFirst(long fc){first_frame = fc;}
      public void   setLast(long fc){last_frame = fc;}
      public void   setSlope(double s){slope = s;}
      public void   setIntercept(double i){intercept = i;}

      public long   getFirst(){return first_frame;}
      public long   getLast(){return last_frame;}
      public double getSlope(){return slope;}
      public double getIntercept(){return intercept;}
   }

   //declare an array of time pairs
   private TimeRec[] time_recs;
   private int time_rec_cnt = 0;

   private Map<Integer, LinModel> models;
   private Map<Integer, Long> epochs;

   public ExtractTiming(FrameHolder frameHolder){
      this.frames     = frameHolder.getFrames();
      this.numFrames  = frameHolder.getNumFrames();
      this.fcRange    = frameHolder.getFcRange();
      this.time_recs  = new TimeRec[frameHolder.getNumRecords("mod4")];
      this.models     = new HashMap<Integer, LinModel>();
      this.epochs     = new HashMap<Integer, Long>();
   }

   public int getTimeRecs(){
      int
         current_week = 0, week, pps;
      int
         fc_offset, frame_i, rec_i;
      long
         ms, mod4;
      Long
         fc;

      //get the initial values for current_week
      for(frame_i = 0; frame_i < this.numFrames; frame_i++) {
         current_week = this.frames[frame_i].getWeek();

         if(current_week != HKPG.WEEK_FILL){
            break;
         }
      }
      if(current_week == 0 || current_week == HKPG.WEEK_FILL) {
         CDF_Gen.log.writeln("Could not get week variable for dataset");
         return 0;
      }

      //cycle through the entire data set and create an array of time records
      for(frame_i = 0, rec_i = 0; frame_i < this.numFrames; frame_i++){

         //check if this is a frame which contains the GPS time data 
         if(this.frames[frame_i].mod4 != Ephm.TIME_I){continue;}

         //make sure all the time components are valid
         fc = this.frames[frame_i].getFrameCounter();
         if(fc == null || fc == BarrelCDF.FC_FILL){
            continue;
         }

         ms = this.frames[frame_i].getGPS();
         if((ms < MINMS) || (ms > MAXMS)){
            continue;
         }

         pps = this.frames[frame_i].getPPS();
         if((pps < MINPPS) || (pps > MAXPPS)){
            //check if pps is high because it came super early so the
            //dpu didnt have a chance to write "0"
            if(pps == 65535 || pps == 32768){pps = 0;} 
            else{continue;}
         }

         //get week data if this frame has it.
         //If not, use the current_week variable
         week = 
            this.frames[frame_i].mod40 == HKPG.WEEK ?
               this.frames[frame_i].getWeek() : current_week;

         //if the week variable is out of range, use current_week instead
         if((week < MINWK) || (week > MAXWK)){
            week = current_week;
         }

         //create a new time record 
         this.time_recs[rec_i] = new TimeRec(fc.intValue(), ms, week, pps);
         rec_i++;
      }

      //save the number of records that were found
      this.numRecords = rec_i;

      return rec_i;
   }
   
   public void fillModels(){
      int
         last_rec = 0,
         frame_i  = 0,
         model_i  = -1,
         fc       = 0,
         fg       = 0,
         last_fc  = 0,
         mid_frame;
      SimpleRegression
         fit     = null,
         new_fit = null;
      LinModel linModel = null;
      BarrelFrame frame;

      //create a model for each batch of time records
      for(int first_rec = 0; first_rec < this.numRecords; first_rec = last_rec){

         //incriment the last_rec by the max, or however many recs are left
         last_rec += Math.min(MAX_RECS, (this.numRecords - first_rec));

         //try to generate a model
         new_fit = genModel(first_rec, last_rec - 1);

         //Need to add better criteria than this for accepting a new model
         if(new_fit != null){
            fit = new_fit;
            model_i++;

            //create a new linear model object
            linModel = new LinModel();
            linModel.setSlope(fit.getSlope()); 
            linModel.setIntercept(fit.getIntercept()); 
            linModel.setFirst(time_recs[first_rec].getFrame()); 
            linModel.setLast(time_recs[last_rec - 1].getFrame()); 

            //find the mid frame number by first getting the first frame
            mid_frame = (int)linModel.getFirst();
            //then bump it up by half the frame range used
            mid_frame += 
              (int)((linModel.getLast() - mid_frame) / 2);

            //associate this model with the fc of the midpoint frame
            this.models.put(mid_frame, linModel);

            System.out.println(
               "Frames " + linModel.getFirst() + " - " + linModel.getLast()); 
            System.out.println(
               "\tm = " + fit.getSlope() + 
               ", b = " + fit.getIntercept() + 
               " slope error = " + fit.getSlopeStdErr() + 
               " n = " + fit.getN()
            );
         } else {
            System.out.println(
               "Failed to get model using " + (last_rec-first_rec) + " records."
            );
         }
      }

      if(model_i == -1){
         //no timing models were ever created. 
         //Use slope=1000 and intercept=0 to use frame number epoch.
         //this will clearly not give a good result for time, but will
         //allow the data to be plotted as a time series.
         //This will be a place to add a quality flag
         model_i++;
         linModel = new LinModel();
         linModel.setSlope(1000);
         linModel.setIntercept(0);
         linModel.setFirst(0);
         linModel.setLast(this.numRecords); 
      }

      //Associate any remaining frames with the last model
      if (linModel != null) {
         mid_frame = (int)linModel.getFirst(); //first set it to the first frame
         mid_frame += //then bump it up by half the frame range used
           (long)((linModel.getLast() - mid_frame) / 2);
         this.models.put(mid_frame, linModel);
      }
   }

/* 
   public void fillEpoch(){
      long fc; 
      int date_offset, size;
      double m, b;
      
      //fill the 1Hz and faster timestamps
      size = data.getSize("1Hz");
      for(int data_i = 0, model_i = 0; data_i < size; data_i++){
         fc = data.frame_1Hz[data_i];

         //verify we have the correct model selected
         model_i = selectModel(fc, model_i);

         //save the model used for this frame
         data.time_model_intercept[data_i] = 
            models[model_i].getIntercept();
         data.time_model_slope[data_i] = 
            models[model_i].getSlope();

         //calculate epoch in ns
         data.epoch_1Hz[data_i] = (long)(
            ((fc * data.time_model_slope[data_i]) + 
            data.time_model_intercept[data_i]) * 1000000
         );

         
         //offset epoch to the begining of the accumulation period
         //data.epoch_1Hz[data_i] -= Constants.SING_ACCUM;
         

         //save epoch to the various time scales
         //fill the >1Hz times 
         for(int fill_i = 0; fill_i < 4; fill_i++){
            data.epoch_4Hz[(data_i * 4) + fill_i] = 
               data.epoch_1Hz[data_i] + (fill_i * 250000000);
         }
         for(int fill_i = 0; fill_i < 20; fill_i++){
            data.epoch_20Hz[(data_i * 20) + fill_i] = 
               data.epoch_1Hz[data_i] + (fill_i * 50000000);
         }
      }

      //fill mod4 timestamps
      size = data.getSize("mod4");
      for(int data_i = 0, model_i = 0; data_i < size; data_i++){
         fc = data.frame_mod4[data_i];
         model_i = selectModel(fc, model_i);
         data.epoch_mod4[data_i] = (long)(
            ((fc * models[model_i].getSlope()) + 
            models[model_i].getIntercept()) * 1000000
         );

         data.epoch_mod4[data_i] -= MSPC_EPOCH_OFFSET;
      }

      //fill mod32 timestamps
      size = data.getSize("mod32");
      for(int data_i = 0, model_i = 0; data_i < size; data_i++){
         fc = data.frame_mod32[data_i];
         model_i = selectModel(fc, model_i);
         data.epoch_mod32[data_i] = (long)(
            ((fc * models[model_i].getSlope()) + 
            models[model_i].getIntercept()) * 1000000
         );

         data.epoch_mod32[data_i] -= SSPC_EPOCH_OFFSET;
      }

      //fill mod40 timestamps
      size = data.getSize("mod40");
      for(int data_i = 0, model_i = 0; data_i < size; data_i++){
         fc = data.frame_mod40[data_i];
         model_i = selectModel(fc, model_i);
         data.epoch_mod40[data_i] = (long)(
            ((fc * models[model_i].getSlope()) + 
            models[model_i].getIntercept()) * 1000000
         );
        
         
         //offset the epoch to the accumulation time of the first frame
         //data.epoch_mod40[data_i] -= (((fc % 40) + 1) * Constants.SING_ACCUM);
         
      }
   }
*/

   public void fixWeekOffset(){
      /*
      Because the each "day" of data most likely contains some portion of a 
      call that started before 00:00 and/or lasted until after 23:59 of the 
      current day, we might have a data set that spans the Sat/Sun week boundry.
      The week variable is only transmitted once every 40 frames, so there is 
      the potential for the epoch variable to jump back when the ms_of_week 
      rolls over.
      */
      
      int
         initial_week = this.time_recs[0].getWeek(),
         week;
      long
         initial_ms = this.time_recs[0].getMSW(),
         msw;
      
      //start looking for rollover
      for(int rec_i = 0; rec_i < this.numRecords; rec_i++){
         //get the msw and week for this record
         msw  = this.time_recs[rec_i].getMSW();
         week = this.time_recs[rec_i].getWeek();

         //check to see if the ms_of_week rolled over
         //the value given by the gps might jump around a bit, so make sure 
         //the roll back is significant (>1min)
         if((msw - initial_ms) < -60000){
            //check if the week variable was updated
            if(week == initial_week){
               //the week variable has not yet updated,
               // add 1 week of ms to the ms_of_week variable
               this.time_recs[rec_i].setMSW(msw + 604800000);
            }
         }
      }
   }

   public long getEpoch(long fc) {
      return getEpoch((int)fc);
   }
   public long getEpoch(int fc) {
      Long epoch = this.epochs.get(fc);
      
      return (epoch == null ? calcEpoch(fc) : epoch);
   }

   public long calcEpoch(int target) {
      long epoch;
      LinModel model;
      int
         fc,
         prev_fc = 0,
         next_fc = 0;
      Iterator<Integer> prev_fc_i = this.models.keySet().iterator();
      Iterator<Integer> next_fc_i = this.models.keySet().iterator();
      next_fc_i.next();

      //fc_i is sorted so the earliest fc will come first. 
      //We want to scan through all fc's that have peaks until we find
      //find the first peak with an fc larger than the target fc 
      while (next_fc_i.hasNext()) {
         prev_fc = prev_fc_i.next();
         next_fc = next_fc_i.next();
         if(target <= next_fc){
            break;
         }
      }

      //select whichever fg is closest to the target fc
      fc = ((next_fc - target) > (target - prev_fc)) ? prev_fc : next_fc;
      model = this.models.get(fc);

      //calculate epoch in ns
      epoch = (long)(
         ((target * model.getSlope()) + model.getIntercept()) * 1000000
      );
     
      //save the epoch for next time
      this.epochs.put(fc, epoch);

      return epoch;
   }
/*
   private int selectModel(final long fc, final int i){
      int model_i = i;
      //select a model for this frame
      if(fc > models[model_i].getLast()){
         //frame came after the last valid fc for the current model
         for(int new_i = 0; new_i < model_cnt; new_i++){
            //loop through the remaining models
            if(fc <= models[new_i].getLast()){
               //stop looping when we find a model that has a 
               //fc range containing this frame
               model_i = new_i;
               break;
            }
         }
      }

      return model_i;
   }
*/
   
   private SimpleRegression genModel(int first, int last){
      int cnt = last - first;
      double[] offsets = new double[cnt];
      double med;
      SimpleRegression fit = new SimpleRegression();

      //not enough pairs to continue
      if(cnt < 3){return null;}
      
      //get offsets from set of time pairs
      //"offsets" are the difference between the nominal time guess and
      //the time that was transmitted
      for(int rec_i = first, offset_i = 0; rec_i < last; rec_i++, offset_i++){
         offsets[offset_i] = 
            this.time_recs[rec_i].getMS() - 
            (NOM_RATE * this.time_recs[rec_i].getFrame());
      }
      
      //find the median offset value
      med = median(offsets);

      //Find all the points that are within 200ms from the median offset
      //and add them to the model
      for (int rec_i = first, offset_i = 0; rec_i < last; rec_i++, offset_i++){
         if(Math.abs(offsets[offset_i] - med) < 200){
            fit.addData(
               this.time_recs[rec_i].getFrame(),
               this.time_recs[rec_i].getMS()
            );
         }
      }
      return fit;
   }
   
   private double median(double[] list){
      double[] sortedList = new double[list.length];
      
      //copy the input list and sort it
      System.arraycopy(list, 0, sortedList, 0, list.length);
      
      Arrays.sort(sortedList);
      
      if(list.length > 2){
         return sortedList[(int) (sortedList.length / 2) + 1];
      }else{
         return sortedList[0];
      }
   }
}
