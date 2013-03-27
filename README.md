switchopen
==========

This is some old software written to make things quicker at a previous job.  It was written in spare time at home.  

Purpose:  

The purpose of this application was to increase the efficiency of the workflow at a previous place of employment.  Standard process involved tasks which identified equipment by unique property tags. Then a search through a massive paper network map was performed to find the equipment and get the address in order to login in and perform whatever task was needed.  This replaced the tedious and time consuming effort by allowing the put the unique identifier, an address or any other part of the name of the equipment and automatically log into it using the credentials supplied at the beginning of the session.

A secondary function is the mac address converter.  Cisco User Tracking Utility required mac addresses to be in a certain format, while three formats were in use.  This function would take any of those three and convert it for use in the user tracking utility.

How to use:  

User credentials are added in the drop down menu so that equipment requirements, if any will have access to them in order to complete login.  Type anything that may be unique about the device name or the IP address in the tag number entry field and click on login or type Enter.  If the device is listed and a DNS entry exists for it, a ssh shell will open up and automatic login to the device will occur.  Alternately, if you need a mac address converted to the format required for the Cisco User Tracker Utility, place it in the mac entry field and click login or type Enter.  There is also drop-down dialog for importing a file for the listing of devices, in CSV format.

Operation:  

  This application is dependent on a CSV file that contains the names of the network equipment as they appear in DNS.  Because of the design of the network that this application was used in, DNS entries exist.  Furthermore, network monitoring equipment that was in use at the time had names that were identical to the DNS names.  The equipment list was exported from the network monitoring equipment and used for the purposes of this application.  This only needed to happen if there were changes to the equipment names and only to one host.  Background network updates were implemented because twenty four hour staffing existed and not everyone was available to receive the updates via email, etc.  Periodic broadcasts would send date/time stamps and hashes of the file to see what user had the latest version, and then automatically update if the machine was in the list of machines that were allowed to send or receive updates.  Later versions included a small table of hostnames in the broadcast.  The host  could then update its address only.  The host would then broadcast the table out to others so that the remote hosts tables could be updated.  This was needed because dynamic DNS was broken for a period of time and people using this application could not receive updates for network equipment names.  

	Because of restrictions on the use of multicast network space, multicast was not implemented for this application.  Instead there are two versions of this application.  One version performs local network broadcasts only.  It was used by the majority of the people at this place of employment.  The other version uses the allowed-hosts list to broadcast to the specific remote hosts.  This version is the nnm version of the application.  This version was used on a few specific monitoring hosts because of their placement and that they were common use hosts for level one troubleshooting and discovery of issues on the network.

