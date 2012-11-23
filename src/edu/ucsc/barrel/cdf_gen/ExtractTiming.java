package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/*
ExactTiming.java v12.10.22

Description:
   Uses a block of gps time info to create a more exact time variable.
   Ported from MPM's C code.

v12.11.22
   -Added an extra iterating variable to correctTime() so timeRecs indexed correctly
   
v12.11.20
   -Changed references to Level_Generator to CDF_Gen
   
v12.10.15
   -First Version

*/

public class ExtractTiming {
   //Set some constant values
   
   // OFFSET is used to reference the calculated
   // milliseconds time to something other than the default
   // start of gps time (06Jan1980).
   private static final double OFFSET = 914803200.0;// (01Jan2009 - 06Jan1980) in seconds
   private static final int MAX_RECS = 2000;// max number of frames into model           
   private static final double NOM_RATE = 999.89;// nominal ms per frame
   private static final double SPERWEEK = 604800.0;// #seconds in a week
   private static final byte FILLED = 1;//quality bit---ms time filled
   private static final byte NEW_WEEK = 2;// quality bit---replaced week
   private static final byte NEW_MSOFWEEK = 4;// quality bit---replaced msofweek
   private static final short BADFC = 256;// quality bit---bad fc value 
   private static final short BADWK = 512;// quality bit---bad week value
   private static final short BADMS = 1024;// quality bit---bad msofweek
   private static final short BADPPS = 2048;// quality bit---bad PPS
   private static final short NOINFO = 4096;// quality bit---not enough info
   private static final long MSFILL = 0xFFFFFFFF;// fill value for ms_of_week
   private static final byte WKFILL = 0;// fill value for week
   private static final short MINWEEK = 1200;
   private static final short MAXWEEK = 1880;

   //model parameters for a linear fit
   //Example: ms = rate * (fc + offset);
   public class Model{
      private double rate; 
      private double offset;
      
      public Model(double r, double o){
         rate = r;
         offset = o;
      }
      
      public void setRate(double r){rate = r;}
      public void setOffset(double o){offset = o;}
      
      public double getRate(){return rate;}
      public double getOffset(){return offset;}
   }
   
   private class TimePair{
      private double ms;// frame time; ms after GPS 00:00:00 1 Jan 2010
      private long fc;//frame counter

      public void setTime(double t){ms = t;}
      public void setFrame(long f){fc = f;}
      
      public double getTime(){return ms;}
      public long getFrame(){return fc;}
   }
   
   private class BarrelTime{
      private double ms = -1.0;//frame time; ms after GPS 00:00:00 1 Jan 2010
      private long fc = -1;//frame counter
      private double ms_of_week = -1.0;// ms since 00:00 on Sunday
      private long week = -1;//weeks since 6-Jan-1980
      private long pps = -1;//ms into frame when GPS pps comes
      private short quality = 0;//quality of recovered time
      private long flag = -1;//unused for now
     
      public void setMS(double t){ms = t;}
      public void setFrame(long f){fc = f;}
      public void setMS_of_week(double msw){ms_of_week = msw;}
      public void setWeek(long w){week = w;}
      public void setPPS(long p){pps = p;}
      public void setQuality(short q){quality |= q;}
      public void setFlag(long f){flag = f;}
      
      public double getMS(){return ms;}
      public long getFrame(){return fc;}
      public double getMS_of_week(){return ms_of_week;}
      public long getWeek(){return week;}
      public long getPPS(){return pps;}
      public short getQuality(){return quality;}
      public long getFlag(){return flag;}
   }
   
   //set initial time model
   private Model time_model = new Model(1.0, 0.0);
   //set an array of time pairs
   private TimePair[] time_pairs = new TimePair[MAX_RECS];
   
   public void fillTime(BarrelTime[] recs, int last_rec){
      double temp;
      Model q;
      int pair_cnt, good_cnt, goodfit, 
         remaining_recs, rec_end_i, rec_start_i;

      if (!check(recs, last_rec)) return;

      rec_start_i = 0;
      remaining_recs = last_rec;
      while (remaining_recs > 0) {
         //find the end of the array for this pass 
         System.out.println(remaining_recs + " remaining...");
         rec_end_i = Math.min(MAX_RECS, remaining_recs); 
         
         pair_cnt = makePairs(recs, rec_start_i, rec_end_i);
      System.out.println(pair_cnt + " pairs...");
         if (pair_cnt < 2) {
            if (evaluateModel(time_model, pair_cnt)){
               updateData(recs, rec_start_i, rec_end_i);
            }
            else{
               for (int rec_i = rec_start_i; rec_i < rec_end_i; rec_i++){
                  recs[rec_i].setQuality(NOINFO);
               }
            }
         }else{
            good_cnt = selectPairs(pair_cnt);
System.out.println("good_cnt = " + good_cnt);
            if (good_cnt > 2) {
               //Improve condition
               q = genModel(good_cnt);
               if (evaluateModel(q, good_cnt)) {
                  time_model.setRate(q.getRate());
                  time_model.setOffset(q.getOffset());
               }
            }else{
               ;
               //FINISH THIS
            }
            updateData(recs, rec_start_i, rec_end_i);
         }
         // printf("Using model time(ms) = %17.13lf(fc + %19.9lf)\n",
         //model.rate, model.offset);
         remaining_recs -= rec_end_i;
         rec_start_i += rec_end_i;
      }
   }
   
   public int makePairs(BarrelTime[] recs, int start, int end){
      int goodcnt = 0;
      int cnt;
      
      //Make sure there are a good number of records
      cnt = end - start;
      if (cnt <= 0 || cnt > MAX_RECS){return 0;}

      for(int rec_i = start; rec_i < end; rec_i++) {
         if(recs[rec_i].getMS_of_week() != MSFILL 
            && recs[rec_i].getWeek() != WKFILL
         ) {
			 System.out.println(rec_i);
            if (recs[rec_i].getPPS() < 241) {
               time_pairs[goodcnt].setTime( 
                  1000 * (SPERWEEK * recs[rec_i].getWeek() - OFFSET) +
                  recs[rec_i].getMS_of_week() - recs[rec_i].getPPS()
               );
            } else {
               time_pairs[goodcnt].setTime(
                  1000 * (SPERWEEK * recs[rec_i].getWeek() + 1 - OFFSET) +
                  recs[rec_i].getMS_of_week() - recs[rec_i].getPPS()
               );
            }
            time_pairs[goodcnt].setFrame(recs[rec_i].getFrame());
            goodcnt++;
         }
      }
      return goodcnt;
   }
   
   public boolean check(BarrelTime[] recs, int last){
      boolean status = true; //false means the list of times was rejected
      
      if (last < 1){
         //reject the list of times if it is empty 
         return false;
      }

      for(int rec_i = 0; rec_i < last; rec_i++) {
         if (recs[rec_i].getFrame() < 0) {
            recs[rec_i].setQuality(BADFC);
            status = false;
         }
         if (recs[rec_i].getWeek() != WKFILL && 
            (recs[rec_i].getWeek() < MINWEEK || recs[rec_i].getWeek() > MAXWEEK)
         ) {
            recs[rec_i].setQuality(BADWK);
            recs[rec_i].setWeek(WKFILL);
         }
         if (
            recs[rec_i].getMS_of_week() != MSFILL &&
            (
               recs[rec_i].getMS_of_week() < 0 || 
               recs[rec_i].getMS_of_week() > (SPERWEEK * 1000)
            )
         ) {
            recs[rec_i].setQuality(BADMS);
            recs[rec_i].setMS_of_week(MSFILL);
         }
         if (recs[rec_i].getPPS() == 0xFFFF){recs[rec_i].setPPS(0);}
         
         if (recs[rec_i].getPPS() > 1000){recs[rec_i].setQuality(BADPPS);}
      }
      
      return status;
   }
   
   public int selectPairs(int m){
      double[] offsets = new double[m], qoffsets = new double[m];
      double med;
      int last_pair_i;

      //not enough pairs to continue
      if (m < 2){return m;}
      
      //get offsets from set of time pairs
      for (int pair_i = 0; pair_i < time_pairs.length; pair_i++){
         qoffsets[pair_i] = offsets[pair_i] = 
            time_pairs[pair_i].getTime() - 
            (NOM_RATE * time_pairs[pair_i].getFrame());
      }
      
      med = median(qoffsets);

      last_pair_i = 0;
      for (int pair_i = 0; pair_i < m; pair_i++) {
         if (Math.abs(offsets[pair_i] - med) > 200.0){continue;}
         
         if (last_pair_i != pair_i){
            time_pairs[last_pair_i] = time_pairs[pair_i];
         }
         
         last_pair_i++;
      }
      
      return last_pair_i;
   }
   
   public double median(double[] list){
      double[] sortedList = new double[list.length];
      
      //copy the input list and sort it
      System.arraycopy(list, 0, sortedList, 0, list.length);
      
      Arrays.sort(sortedList);
      
      return sortedList[(sortedList.length/2) + 1];
   }
   
   public Model genModel(int n){
      Model linfit = new Model(0.0,0.0);
      double  mux = 0., muy = 0., xy = 0., xx = 0.;
      double m, x, y;

      //not enough points to generate model
      if (n < 3) {return null;}

      //get mean x and y values
      for (int pnt_i = 0; pnt_i < n; pnt_i++) {
         mux += time_pairs[pnt_i].getFrame();
         muy += time_pairs[pnt_i].getTime();
      }
      mux /= n;
      muy /= n;
      
      //calculate the least square regression
      for (int pnt_i = 0; pnt_i < n; pnt_i++) {
         x = time_pairs[pnt_i].getFrame() - mux;
         y = time_pairs[pnt_i].getTime() - muy;
         xx += x * x;
         xy += x * y;
      }
      m = xy / xx;
      
      //save the model
      linfit.setRate(m);
      linfit.setOffset(muy / m - mux);
      
      return linfit;
   }

   public boolean evaluateModel(Model test_model, int cnt){
      if (test_model == null || cnt == 0){
         return false;
      }
      else if (cnt == 1) {
         double ms1 = 
            test_model.getRate() * 
               (time_pairs[0].getFrame() + test_model.getOffset());
         double ms2 = 
            time_model.getRate() * 
               (time_pairs[0].getFrame() + time_model.getOffset());
      
         //test to see if model gives less than 50ms change
         if(Math.abs(ms1 - ms2) < 0.5 && 
            Math.abs(ms2 - time_pairs[0].getTime()) < 0.5
         ){
            return true;
         }
         
         else{return false;}
      }
      else{
          //FIX THIS
          return true;
       }
   }
   
   public void updateData(BarrelTime[] recs, int start, int end){
      double ms, sec;
      long week, msofweek;

      for(int time_i = start; time_i <= end; time_i++) {
         ms = 
            time_model.getRate() * 
            (recs[time_i].getFrame() + time_model.getOffset());
         sec = ms / 1000. + OFFSET;
         week = (long) (sec / SPERWEEK);
         msofweek = (long) (1000. * Math.floor(sec - week * SPERWEEK));
         
         if (msofweek != recs[time_i].getMS_of_week()) {
            recs[time_i].setQuality(NEW_MSOFWEEK);
            recs[time_i].setMS_of_week(msofweek);
         }
         
         if (week != recs[time_i].getWeek()) {
            recs[time_i].setQuality(NEW_WEEK);
            recs[time_i].setWeek(week);
         }
         
         recs[time_i].setMS(ms);
         recs[time_i].setQuality(FILLED);
      }
   }
   
   public BarrelTime[] correctTime(DataHolder data){
      int temp, day, fc, week, ms, pps, cnt;
      BarrelTime[] timeRecs = new BarrelTime[MAX_RECS];
      
      //data set index reference
      int FC = 0, DAY = 1, WEEK = 2, MS = 3, PPS = 4;
      int rec_i = 0; //keep track of current record in BarrelTime object array
      
      for(int data_i = 0; data_i < data.getSize(); rec_i++, data_i++){
         //check if the BarrelTimes array is full
         if(rec_i == MAX_RECS ){
            //generate a model and refill the array with adjusted time
            fillTime(timeRecs, rec_i);
            timeRecs = new BarrelTime[MAX_RECS];
            System.out.println("Using model time(ms) = " + time_model.rate +
               "(fc + " + time_model.offset + ")\n");
         
		    rec_i = 0;
         }
         
         //initialize the BarrelTime object
         timeRecs[rec_i] = new BarrelTime();
         
		 //fill a BarrelTime object with data values
         timeRecs[rec_i].setFrame(data.frameNum[data_i]);
         timeRecs[rec_i].setWeek(data.weeks[data_i]);
         
         if(data.ms_of_week[data_i] == 0){ 
            timeRecs[rec_i].setMS_of_week(MSFILL);
         }else{
            timeRecs[rec_i].setMS_of_week(data.ms_of_week[data_i]);
         }
         timeRecs[rec_i].setPPS(data.pps[data_i]);
      }

      //process any remaining records
      fillTime(timeRecs, rec_i);
      
      //null out the BarrelTime array
      timeRecs = null;

      System.out.println("Using model time(ms) = " + time_model.rate +
         "(fc + " + time_model.offset + ")\n");
      System.out.println("Time extracted.\n");
      
      return timeRecs;
  }
}
