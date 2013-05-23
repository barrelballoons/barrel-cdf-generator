/*
ExactTiming.java v13.01.04

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

import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.util.CDFTT2000;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class ExtractTiming {
   //Set some constant values
   private static final int MAX_RECS = 500;// max number of mod4 recs for model
   private static final double NOM_RATE = 999.89;// nominal ms per frame
   private static final int MSPERWEEK = 604800000;// mseconds in a week
   private static final short PPSFILL = -32768;// fill value for PPS
   private static final int MSFILL = -2147483648;// fill value for ms_of_week
   private static final int FCFILL = -2147483648;// fill value for frame counter
   private static final short WKFILL = -32768;// fill value for week
   private static final short MINWK = 1200;
   private static final byte MINPPS = 0;
   private static final byte MINMS = 1;
   private static final byte MINFC = 0;
   private static final short MAXWK = 1880;
   private static final short MAXPPS = 1000;
   private static final int MAXMS = 604800000;
   private static final int MAXFC = 2097152;
   private static final long LEAPMS = 16000;
   
   //quality flags
   private static final short FILLED = 1;
   private static final short NOINFO = 2;
   private static final short BADMOEL = 4;
   private static final short BADFC = 8;
   private static final short BADMS = 16;
   private static final short BADWK = 32;
   private static final short BADPPS = 64;

   private long today;
   private int time_rec_cnt = 0;
   private SimpleRegression time_model;

   //date offset info
   //Offset in ms from system epoch to gps start time (00:00:00 1980-01-60 UTC) 
   private static long GPS_START_TIME; 
   
   //ms from system epoch to J2000 (11:58:55.816 2000-01-01 UTC)
   private static long J2000; 
   
   //ms from GPS_START_TIME to J2000
   private static long J2000_OFFSET;
      
   private class TimeRec{
      private long ms;//frame time; ms since J2000
      private long frame;//frame counter

      public TimeRec(long fc, long msw, short weeks, short pps){
         //figure out if we need to add an extra second based on the PPS
         int extra_ms = (pps < 241) ? 0 : 1000;
         
         //get the number of ms between GPS_START_TIME and start of this week
         long weeks_in_ms = (long)weeks * (long)MSPERWEEK;

         //save the frame number
         frame = fc;

         //calculate the number of milliseconds since J2000 
         ms = 
            weeks_in_ms + msw + extra_ms - pps - LEAPMS - J2000_OFFSET;
      }
      
      public long getMS(){return ms;}
      public long getFrame(){return frame;}
   }
   
   private class LinModel{
      private long first_frame, last_frame;
      private double slope, intercept;

      public void setFirst(long fc){first_frame = fc;}
      public void setLast(long fc){last_frame = fc;}
      public void setSlope(long s){slope = s;}
      public void setIntercept(long i){intercept = i;}

      public long getFirst(){return first_frame;}
      public long getLast(){return last_frame;}
      public double getSlope(){return slope;}
      public double getIntercept(){return intercept;}
   }

   //declare an array of time pairs
   private TimeRec[] time_recs;
   //holder for external data set
   private DataHolder data;
   
   public ExtractTiming(String d){
      //save today's date
      today = Long.valueOf(d);
      
      //get DataHolder storage object
      data = CDF_Gen.getDataSet();
      
      //calculate GPS_START_TIME, J2000, and the offset between them
      Calendar gps_start_cal = 
         Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      gps_start_cal.set(
         1980, 00, 06, 00, 00, 00);
      GPS_START_TIME = gps_start_cal.getTimeInMillis();
      Calendar j2000_cal = 
         Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      j2000_cal.set(
         2000, 00, 01, 11, 58, 55);
      j2000_cal.add(Calendar.MILLISECOND, 816);
      J2000 = j2000_cal.getTimeInMillis();
      J2000_OFFSET = J2000 - GPS_START_TIME;

      int temp, day, fc, week, ms, pps, cnt, mod40;
      int rec_i = 0, frame_i = 0;
      
      time_recs = new TimeRec[data.getSize("mod4")];
   }

   public void getTimeRecs(){
      int fc_offset, rec_mod40_i, rec_1Hz_i;
      short week, pps;
      long ms, fc;

      //cycle through the entire data set and create an array of time records
      for(int rec_mod4_i = 0; rec_mod4_i < time_recs.length; rec_mod4_i++){
         //make sure we have a valid ms
         ms = data.ms_of_week[rec_mod4_i];
         if((ms < MINMS) || (ms > MAXMS)){continue;}
         
         //get initial fc from the mod4 framegroup
         fc = data.frame_mod4[rec_mod4_i]; //last good fc from this mod4 group
         if((fc < MINFC) || (fc > MAXFC)){continue;}

         //figure out the offset from mod4 fc and 1Hz fc
         fc -= ((fc % 4) - DataHolder.TIME); 
         
         //get the indices of other cadence data
         rec_1Hz_i = data.convertIndex(rec_mod4_i, fc, "mod4", "1Hz");
         rec_mod40_i = data.convertIndex(rec_mod4_i, fc, "mod4", "mod40");

         //figure out which pps to use
         pps = (short)data.pps[rec_1Hz_i];
         if((pps < MINPPS) || (pps > MAXPPS)){continue;}

         //get number of weeks since GPS_START_TIME
         week = (short)data.weeks[rec_mod40_i];
         if((week < MINWK) || (week > MAXWK)){continue;}
         
         System.out.println(fc + ",  " + ms);

         time_recs[time_rec_cnt] = new TimeRec(fc, ms, week, pps);
         time_rec_cnt++;
      }
   }
   
   public void fillModels(){
      int last_rec = 0, frame_i = 0, size_1Hz;
      long last_frame;
      SimpleRegression fit = null, new_fit = null;
      
      size_1Hz = data.getSize("1Hz");

      //create a model for each batch of time records
      for(
         int first_rec = 0; 
         first_rec < time_rec_cnt; 
         first_rec = last_rec + 1
      ){
         //incriment the last_rec by the max, or however many recs are left
         last_rec += Math.min(MAX_RECS, (time_rec_cnt - first_rec));
         
         //try to generate a model
         new_fit = genModel(first_rec, last_rec);
         
         //Need to add better criteria than this for accepting a new model
         if(new_fit != null){
            fit = new_fit;
            System.out.println(
               "Frames " + time_recs[first_rec].getFrame() + " - " +
               time_recs[last_rec].getFrame() + " \n" +
               "\tm = " + fit.getSlope() + ", b = " + fit.getIntercept()
            );
         }
         
         //fill each 1Hz record with the current model
         if(fit != null){
            last_frame = time_recs[last_rec].getFrame();
            while(
               (data.frame_1Hz[frame_i] <= last_frame) && 
               (frame_i < size_1Hz)
            ){
               data.time_model_slope[frame_i] = fit.getSlope(); 
               data.time_model_intercept[frame_i] = fit.getIntercept(); 
               frame_i++;
            }
         }
      }

      //fill any remaining 1Hz records with the last model
      while(frame_i < size_1Hz){
            data.time_model_slope[frame_i] = fit.getSlope(); 
            data.time_model_intercept[frame_i] = fit.getIntercept(); 
            frame_i++;
      }
   }

   public void fillEpoch(){
      int 
         fc_mod4 = 0, fc_mod32 = 0, fc_mod40 = 0, 
         last_fc_mod4 = -1, last_fc_mod32 = -1, last_fc_mod40 = -1,
         rec_num_mod4 = -1, rec_num_mod32 = -1, rec_num_mod40 = -1;
      
      long[] tt2000_parts;
      long rec_date, current_frame;
      int date_offset, size;

      size = data.getSize("1Hz");
      for(int data_i = 0; data_i < size; data_i++){
         fc_mod4 = 
            data.frame_1Hz[data_i] - (data.frame_1Hz[data_i] % 4);
         fc_mod32 = 
            data.frame_1Hz[data_i] - (data.frame_1Hz[data_i] % 32);
         fc_mod40 = 
            data.frame_1Hz[data_i] - (data.frame_1Hz[data_i] % 40);

         //increment the record number for the <1Hz cadence data types
         if(fc_mod4 != last_fc_mod4){rec_num_mod4++;}
         if(fc_mod32 != last_fc_mod32){rec_num_mod32++;}
         if(fc_mod40 != last_fc_mod40){rec_num_mod40++;}

         //convert from "ms since J2000" to "ns since J2000"
         data.epoch_1Hz[data_i] = (long)(
            ((data.frame_1Hz[data_i] * data.time_model_slope[data_i]) + 
            data.time_model_intercept[data_i]) * 
            1000000
         );

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

         //fill the <1Hz times. 
         //These time stamps are for the begining of the accumulation period
         data.epoch_mod4[rec_num_mod4] = (long)data.epoch_1Hz[data_i];
         data.epoch_mod32[rec_num_mod32] = (long)data.epoch_1Hz[data_i];
         data.epoch_mod40[rec_num_mod40] = (long)data.epoch_1Hz[data_i];

         //make sure we have a valid epoch value
         if(data.epoch_1Hz[data_i] > 0){
            //check for date rollover
            tt2000_parts = CDFTT2000.breakdown(data.epoch_1Hz[data_i]);
            rec_date = 
               tt2000_parts[2] + //day
               (100 * tt2000_parts[1]) + //month
               (10000 * (tt2000_parts[0] - 2000)); //year

            /*
            Save the current record number to the 
            correct spot in the day_rollovers array.
            This indicates the last record for each day.
            The +1 is needed to have the indicies match the 
            constants set in DataHolder.YESTERDAY/TODAY/TOMORROW
            */
            date_offset = (int)(rec_date - today + 1);
            data.day_rollovers[date_offset] = data_i;
         }

         last_fc_mod4 = fc_mod4;
         last_fc_mod32 = fc_mod32;
         last_fc_mod40 = fc_mod40;
      }
   }

   public void fixWeekOffset(){
      /*
      Because the each "day" of data most likely contains some portion of a 
      call that started before 00:00 and/or lasted until after 23:59 of the 
      current day, we might have a data set that spans the Sat/Sun week boundry.
      The week variable is only transmitted once every 40 frames, so there is 
      the potential for the epoch variable to jump back when the ms_of_week 
      rolls over.
      */
      
      int initial_week = 0, initial_ms = 0;
      
      //start looking for rollover
      for(int ms_i = 0; ms_i < data.getSize("mod4"); ms_i++){
         //try to find and initial set of 
         //timestamps and week variables if needed.
         if(initial_week == 0){initial_week = data.weeks[ms_i];}
         if(initial_ms == 0){initial_ms = data.ms_of_week[ms_i/10];}

         //check to see if the ms_of_week rolled over
         //the value given by the gps might jump around a bit, so make sure 
         //the roll back is significant (>1min)
         if((data.ms_of_week[ms_i] - initial_ms) < -60000){
            //check if the week variable was updated
            if(data.weeks[ms_i/10] != 0 && data.weeks[ms_i/10] == initial_week){
               //the week variable has not yet updated,
               // add 1 week of ms to the ms_of_week variable
               data.ms_of_week[ms_i] += 604800000;
            }
         }
      }
   }

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
            time_recs[rec_i].getMS() - 
            (NOM_RATE * time_recs[rec_i].getFrame());
      }
      
      //find the median offset value
      med = median(offsets);

      //Find all the points that are within 200ms from the median offset
      //and add them to the model
      for (int rec_i = 0; rec_i < cnt; rec_i++){
         if(Math.abs(offsets[rec_i] - med) < 200){
            fit.addData(time_recs[rec_i].getFrame(), time_recs[rec_i].getMS());
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
