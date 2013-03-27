switchopen
==========

This is some old software written to make things quicker at work.  
It allowed user credentials to be store for the session to log into network equipment using ssh.
This allowed the use of unique identifiers to find hostnames that were identical to DNS names for the equipment.
It requires a csv file that has the DNS names.  Fortunately some of the network monitoring equipment had this,
so the information is merely imported to this application and searches based on open criteria would 
result in a correct DNS name and automatic login could happen.  Background network updates were added so that 
users didn't have to try and maintain the file used.
