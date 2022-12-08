# brew-controller
A brewing controller based on a Raspberry PI developed in Java

![](C:\development\data\git\brew-controller\media\brew_server.png)

## Setup Raspberry Pi
* install dietpi (set static ip)
* dietpi-config
  * i2c enabled (advanced)
  * location/timezone/keyboard
  * change password
* rename user dietpi:
  * usermod -l pi dietpi
  * usermod -d /home/pi -m pi
  * usermod -aG sudo pi
  * usermod -aG adm pi
* dietpi-software: 
  * openssh instead of dropbear
  * sqlite
  * jdk
  * wiringpi
* install jetty 9
    * sudo apt-get install jetty9
    * sudo systemctl enable jetty9
    * make database-path writable for jetty (see also: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=933857)
      * usermod -aG adm pi
      * copy /lib/systemd/system/jetty9.service /etc/systemd/system
      * pico /etc/systemd/system/jetty9.service
        * add ReadWritePaths=/opt/sqlite/databases/
      * copy and expand jetty_lib_ext.tar to <jetty9_home>/lib/ext
      * systemctl daemon-reload
      * systemctl restart jetty9
      * ln -s /usr/share/jetty9/ ~./jetty
* create database location and make it writable
* configure database location inside environment.properties (i.e. /usr/local/etc)
* create symlink to environment.properties at
  * /usr/share/jetty9/
  * /root
  
## Password less login from Client-PC
* cd ~/.ssh
* ssh-keygen -t rsa -m PEM
* ssh-copy-id -i id_rsa.pub pi@<pi_ip_address>

## Add Application
* copy and adjust sample config files to pi
* mvn clean install
* exec target "load-to-pi" from brew-controller/uploadToPi.xml (first adjust keyfile!)
* exec target "load-to-pi" from brew-server/uploadWarToPi.xml (first adjust keyfile!)
## Start Application
* ssh pi@<pi_ip_address>
* sudo java -jar brew-controller.jar -brew
* Go to http://<pi_ip_address>:8080/brew-server/app/

