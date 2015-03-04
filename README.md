# Sandboxed

Android app to fingerprint an environment. 
This project has been a response to recent environment-sensitive Android apps checking if they are running in an emulator or device. 
If the malware detects its running environment as an emulator there is a high probability it is being analyzed in a sandbox. 
This app takes multiple different fingerprints of the environment. 
The goal is to develope in tandumm an emulator that is indistinguishable from a real device to malware.
This research is in collaboration with [Allan Wirth]{https://github.com/allanlw}, [AJ Trainor]{https://github.com/lk86}, [Guanchen Zhang]{https://github.com/naghceuz}.

Features:
--------
* 3 flavors for use on different devices (remote, emulator, device)
* Command and control support for remote fingerprinting
* Multi-threaded scanning
* Automated scanning of Android API and services via reflection, services disabled by default because of permission requirements
* Device flavor includes search and filter capabilities



Configuration
-------------
To remote fingerprint a device:

1. Use this [command and control]{https://github.com/wil3/drop_server}, or use your own and implement the same API
1. Configure *url_c2* property with the url of your command and control server in:
*app/src/main/res/values/strings.xml*
2. To do a full scan set value *simple_scan* in *app/src/main/res/values/bools.xml* to false.
3. If you want to do a simple scan set *simple_scan* to true and then add the classes to scan in the *fp* property in
*app/src/main/res/values/strings.xml*. Reasons to do a simple scan may be due to time restraints of the remote device.

Future Work
-----------
* Perform automated detection of running environment.
* File detection
* Timing detection
* APK, SD card, contact list, etc detection


License
-------

    Copyright (C) 2015 William Koch

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.