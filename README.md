#### ADSBMySQL
This application connects to TCP Port 30003 (or configured alternate port) which is the common Basestation compatible
port number for ADS-B and Mode-S output data. It's a pretty simple program. Purely a hobby endeavor. I have no interest, or enough money in creating a central fusion of data. No replication of the Air Defense Command or DEW Line...

This TCP port does not have any fancy data being output, it is just the most common data that people are interested in - heading, altitude, speed, etc.

The database is updated by a configurable (1 to 13 seconds) time. Aircraft transmit their position every second, but in most cases it isn't necessary to have that resolution. By default I set 3 seconds, which is adequate for me. In the Track Display program, it reads the database every second, which if you set in 13 seconds here, not a lot of its queries will have new data, but reading is not as database intensive as writing. Every write will update several tables.

This update time can be considered a simulated Revolutions Per Minute (RPM) of a rotating radar antenna. A long range radar usually updates every 10 to 12 seconds (6 or 5 RPM), while an airport radar will update every 2.5 seconds (24 RPM). A surface to air missile system will update every second (for comparisons).
```
EDIT: I'm redesigning the computed heading and groundspeed algorithm.
Basically moving it out of the display tracker, and into the database proper.
That way if you have 10 displays up, you don't have 10 applications computing these values.

Plus it will allow me to create a more sophisticated design.
Basically the problem is, that not all ADS-B targets transmit heading and speed.
FedEx mostly, although they seem to be upgrading more and more transponders.

What I do is use the echo positions to calculate the targets speed, since I can easily
measure the distance and the time between each echo.
```
Included is a config file that the program reads on startup. This contains the database table name, and the login credentials. There is also an export of the database so you can initialize the database.

Basically you create a directory to put the ```ADSBMySQL.jar``` file in. Then create a ```lib``` directory and put the database ODBC connector file in there. The program was compiled using this particular file, so don't upgrade it without recompiling (I use Netbeans IDE).

Put the config file in the main directory and you are ready to go after you set up the MySQL database.

If you comment out the GUI off config, a GUI will be displayed and it basically shows the data counts being received by the Basestation TCP port. Otherwise this program does nothing but work in the background storing the data.

![My image](https://raw.githubusercontent.com/srsampson/ADSBMySQL/master/gui.png)

The database is designed so that as new targets come in, their Mode-S ICAO number is added, and the port data is recorded. Sooner or later this aircraft will land or fade-out, and the database will move it to the history file. If it pops up again, then it is issued a new flight number. The data in the ```target``` table then, is the current data. When those targets fade or land, they are deleted from this table, and moved to the ```targethistory``` table.

Also, for ADS-B data with latitude and longitude, a table is available that shows their target echoes, so you could do a database query and plot all the position history data from this aircraft on a map.

There is also a ```metrics``` table that shows how much data has been processed every 30 seconds.

Commandline: ```java -jar ADSBMySQL.jar 2>&1 >>errorlog.txt &``` on Linux and just double click the jar file on Windows.

##### Time and Date Stamps
All data is recorded in UTC time. This is so multiple receivers in different time zones all record to the same time reference. Although, the times may differ if system are not synchronized to GPS.

##### Aircraft Registration
Currently it only updates the registration (N-Number) for USA aircraft, as they are assigned 1:1, and I don't have any info on other countries. I don't do an Internet lookup, as most sites don't allow it.

##### Port 30003 Compatibility
Alas, there are some pretty crummy Port 30003 implementations out there. ```Planeplotter``` for instance doesn't output the Booleans (alert, ident, emergency, etc). These are always ```0```. It's a real piece of crap. Others are worse.

The program to get is ```dump1090``` which along with a $20 receiver will suck data out of the atmosphere and drop it into your database.

##### Mode-S Beast
I am currently using a Mode-S Beast receiver with the dump1090 program (input to port 30004). One thing to keep in mind, is that dump1090 doesn't configure the switches on the Beast. Therefore you should make sure you are feeding the right data. The right data being all the DF (not set for DF17 only for example) and not so important, but usefull, is Mode-A/C enable, as dump1090 can decode these squawks, and maybe in the future they can be used in the database. Right now there is no Mode-A/C use.

##### Dump1090
By using dump1090 you also gain the capability of connecting it to the FlightAware Multilateration (MLAT). This is pretty neat, in that by connecting you will be sharing data with others nearby, and able to track Mode-S aircraft that are not sending out their position. You have to modify the dump1090 to allow MLAT being sent out the Port 30003 (very simple change), and with that you will get more data to put in your database. Note: I don't feed FlightAware anymore, so not sure of the latest changes there.

##### Sample Database View
Here's a sample of what the data might look like.

![My image](https://raw.githubusercontent.com/srsampson/ADSBMySQL/master/screenshot.png)

