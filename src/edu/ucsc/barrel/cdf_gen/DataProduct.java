/*
DataProduct.java

Description:
   Abstract class used for creating data product classes.

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

public abstract class DataProduct{
   protected BarrelCDF cdf;
   
   //functions that deal with interaction with the internal CDF. 
   //These will be the same in all classes
   public BarrelCDF getCDF(){return this.cdf;}
   public void setCDF(BarrelCDF c){this.cdf = c;}
   public void close(){this.cdf.close();}
   public String getCDFPath(){return this.cdf.getPath();}

   //Abstract classes for that will be implemented differently in each class
   protected abstract void addGAttributes();
   protected abstract void addVars();
}
