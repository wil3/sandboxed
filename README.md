# Sandboxed

Android app to finger print an environment.

Features:
--------
* 3 flavors for use on different devices (remote, emulator, device)
* Command and control support for remote fingerprinting
* Multi-threaded scanning
* Automated scanning of Android API and services via reflection
* Device flavor includes search and filter capabilities


Configuration
-------------
To remote fingerprint a device configure url_c2 property with the url of your command and control server in:

```
app/src/main/res/values/strings.xml
```

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