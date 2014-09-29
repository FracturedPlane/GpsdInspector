GpsdInspector can be run using run_linux*.sh * being the architecture of the 
computer that the program is being run on.

If you are on a 64-bit computer and using linux you may need to install the
package ia32-libs. This library helps your 64-bit architecture use 32 bit native
code.

You will need to start gpsd first using the command
gpsd /dev/ttyUSB0 -N -S 2947

You can change the device that gpsd opens with this command. You can also change
the port that gpsd will open in listening mode. the -N option is very important,
it will tell gpsd to accept networked ( non localhost ) connections to gpsd.

This version has been updated to support the new gpsd protocol that was shipped with
gpsd 2.90 and above. You can read about it here 
http://gpsd.berlios.de/client-howto.html#_how_the_gpsd_wire_protocol_works .
