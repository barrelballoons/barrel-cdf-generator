BARREL Data Products and Information
====================================
####Warren Rexroad - 08/2014

##Table of Contents
1. Introduction and Purpose
2. Data Repositories
3. Filenames, Versions and Levels
    1. Data Product Levels
        1. Level Zero
        2. Level One
        3. Level Two
    2. Versions
    3. Filenames
4. Science Data
    1. Fast Spectrum
        1. 2012-2013
        2. 2013-2014
    2. Medium Spectrum
    3. Slow Spectrum 
    4. Magnetometer
6. Housekeeping and Supplementary Data
7. The Epoch Variable

---------------------------------------

1. Introduction and Purpose
============================

The purpose of this document is to give those wanting to use BARREL data a quick guide accessing and understanding the different data types. The discussions will cover what data are stored in the BARREL repositories and any processing that has been performed on them.

2. Data Repositories
====================

There are two main repositories of BARREL data: [CDAWeb](http://cdaweb.gsfc.nasa.gov/cdaweb/sp_phys/) and [BARRELData](http://barreldata.ucsc.edu/data_products). CDAWeb archives stable releases of Level Two data only. BARRELData will contain the most recent versions of all data levels.

###Why use data from CDAWeb?
The data stored at CDAWeb is mostly complete and in a stable state. It is most likely an older version of the data, but it will not change out (or disappear) from underneath you.

###Why use data from BARRELData?
BARRELData is the place where all the testing happens. The most current run of each data set is hosted here for users to test. It should be noted that while each run of our data is mean to be an improvement on the last, it is certainly possible that the most recent data processor changes broke something and the data have suddenly become entirely wrong or have gone missing. 

*Currently there only repository holding data from the 2013-2014 campaign is BARRELData*

3. Filenames, Version, and Levels
=================================

###1. Data Product Levels
#### 1.1 Level Zero
Level Zero products are daily files containing a all raw telemetry streams. A single file will contain all data from each satellite modem call that *start* in a 24hr period. That is, it includes any call originated between 00:00 and 23:59UTC. It is important to understand that the calls are not ended at midnight. A call that is started on January, 1 at 23:00 and last for three hours will be contained (in its entirety) in the January, 1 file. 

Level Zero products are mostly useful as an archival format and as a "pure" source of data for checking the validity of the higher level products.

#### 1.2 Level One
Level One data are stored in CDF files which are grouped by date. Unlike Level Zero files, the dates here are midnight-to-midnight. 

The data contained in these files are essentially the raw values extracted from Level Zero and placed into CDF files. The one bit of processing that was done for these files is time stamp correction. The GPS time that is collected with the BARREL ephemeris data is sent once every four seconds and can be up to one second off. An attempt is made to give each data point a time stamp that is accurate to 1ms. 

Level One CDF files are good choice if you plan to process all of the data manually. 

#### 1.3 Level Two
Like Level One data, the Level Two products are stored in a series of CDF files and grouped by date. In addition to time correction there is a some processing that calibrates the energy spectra and converts raw data values to engineering units. The following list shows how each type of data are processed:
  -Slow and medium spectra are converted from raw counts to counts/keV/