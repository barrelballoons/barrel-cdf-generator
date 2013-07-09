/*
Logger.java

Description:
   Writes output log files
   
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Logger extends File{
   private BufferedWriter bw;

   public Logger(final String filename){
      super(filename);

      //Open the Log file for writing
      try{
         // if file doesnt exists, then create it
         if (!this.exists()) {
            this.createNewFile();
         }

         FileWriter fw = new FileWriter(this.getAbsoluteFile());
         bw = new BufferedWriter(fw);
      }catch(IOException e){
         System.out.println("Could not open log file for writing:");
         System.out.println(e.getMessage());
      }
   }  
   
   public void write(String content){
      try{
         bw.write(content);
      }catch(IOException e){
         System.out.println(e.getMessage());
      }
   }
   public void write(){write("");}
   public void write(int content){write(content + "");}
   public void write(long content){write(content + "");}
   public void write(float content){write(content + "");}
   public void write(double content){write(content + "");}
   public void write(Object content){write(content.toString());}

   public void newLine(){
      try{
         bw.newLine();
      }catch(IOException e){
         System.out.println(e.getMessage());
      }
   }

   public void writeln(String content){
     write(content);
     newLine();
   }
   public void writeln(){writeln("");}
   public void writeln(int content){writeln(content + "");}
   public void writeln(long content){writeln(content + "");}
   public void writeln(float content){writeln(content + "");}
   public void writeln(double content){writeln(content + "");}
   public void writeln(Object content){writeln(content.toString());}

   public void close(){
      try{
         bw.close();
      }catch(IOException e){
         System.out.println("Failed to close log file!");
         System.out.println(e.getMessage());
      }
   }
}
