package edu.ucsc.barrel.cdf_gen;

/*
SpectrumExtract.java v12.11.20

Description:
   Creates energy bin edges and rebins spectra.

v12.11.20
   -Added this documentation
   
*/


public class SpectrumExtract {
   public static double[][] getBinEdges(double scale){
      //edge array index reference
      final int SLOW = 0;
      final int MED = 1;
      final int FAST = 2;
      
      double[][] edges = new double[3][];
      edges[SLOW] = new double[257]; //slow spectrum edges
      edges[MED] = new double[49]; //medium
      edges[FAST] = new double[5]; //fast
      
      int[] widths = {
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
         2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
         4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
         4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
         8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 
         8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 
         16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
         16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 
         32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 
         32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
         64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 
         64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64
      };
      
      //convert bin widths to energy levels
      for(int width_i = 0; width_i < widths.length; width_i++){
         widths[width_i] *= scale;
      }
      
      //fill edge array for slow spectrum
      edges[SLOW][0] = 0.0;
      for(int slow_i = 0; slow_i < 256; slow_i++){
         edges[SLOW][slow_i+1] = edges[SLOW][slow_i] + widths[slow_i];
      }
      
      //fill array for medium spectrum
      edges[MED][0] = edges[SLOW][42];
      int med_i = 0, next_med = 0;
      for(int slow_i = 43; slow_i < 213; slow_i++){
         
         //calculate the next medium index based on the slow index
         next_med = 9 * (slow_i - 42) / 32;
         
         //check to see if the next index is different from the first
         if (med_i != next_med){
            //if we have a new index, save it and copy the energy level
            edges[MED][next_med] = edges[SLOW][slow_i];
            med_i = next_med;
         }
      }
      //set the last energy level
      edges[MED][48] = edges[SLOW][213];
      
      //fill array for fast spectrum
      edges[FAST][0] = 0.0;
      edges[FAST][1] = scale * 75;
      edges[FAST][2] = scale * 230;
      edges[FAST][3] = scale * 350;
      edges[FAST][4] = scale * 620;
      
      return edges;
   }
   
   public static Double[] rebin(
      Integer[] oldVals, double[] oldBins, double[] newBins, boolean flux
   )throws NullPointerException{
      Double[] result = new Double[(newBins.length - 1)];
      
      if (oldBins.length < 2 || newBins.length < 2){
        System.out.println("Rebin array size error!");
        return result;
      }
      
      Double oldLo = oldBins[0];
      Double oldHi = oldBins[1];
      Double newLo = newBins[0];
      Double newHi = newBins[1];
      int newIndex = 0;
      int oldIndex = 0;
      Double total = 0.0;

      while(true){
         if (oldHi <= newLo){
            oldIndex++;
            
            if (oldIndex >= (oldBins.length)) return result;
            
            oldLo = oldHi;
            oldHi = oldBins[oldIndex + 1];
         }else if (newHi <= oldLo){
            
            if(flux) result[newIndex] = total/(newHi-newLo);
            else result[newIndex] = total/(oldHi-oldLo);
            
            total = 0.0;
            newIndex++;
            
            if (newIndex >= (newBins.length - 1)) return result;
            
            newLo = newHi;
            newHi = newBins[newIndex + 1];
            
         }else if (newHi < oldHi){
            total += (newHi - Math.max(oldLo, newLo)) * oldVals[oldIndex];
            
            if(flux) result[newIndex] = total / (newHi - newLo);
            else result[newIndex] = total / (oldHi - oldLo);
            
            total = 0.0;
            newIndex++;
            
            if(newIndex >= newBins.length - 1) return result;
            
            newLo = newHi;
            newHi = newBins[newIndex + 1];
         }else{
            total += (oldHi - Math.max(oldLo, newLo)) * oldVals[oldIndex];
            oldIndex++;
            
            if (oldIndex >= oldVals.length) {
               if(flux) result[newIndex] = total/(newHi-newLo);
               else result[newIndex]=total/(oldHi-oldLo);
               
               return result;
            }
            oldLo = oldHi;
            oldHi = oldBins[oldIndex + 1];
         }
      }
   }
}
