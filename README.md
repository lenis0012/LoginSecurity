LoginSecurity 3 [![Build Status](https://ci.codemc.org/job/lenis0012/job/LoginSecurity/badge/icon)](https://ci.codemc.org/job/lenis0012/job/LoginSecurity/)
=================
Simple, light, fast and secure user authentication management. Since 2012.  
Now even ligher and faster than before!

# Links
- [Development Builds](https://ci.codemc.org/view/Author/job/lenis0012/job/LoginSecurity/)
- [SpigotMC](https://www.spigotmc.org/resources/loginsecurity.19362/)

# Changes in 3.0
* Lightweight download (over 20x smaller than v2.1)
* Improved performance, resulting in higher tps
* Removed AutoIn support, consider migrating to FastLogin
* Removed deprecated hashing algorithms
* Migrated from mcstats to bstats for statistics
* Block opening inventory while not logged in
* Fix errors when using some NPC plugins (like FakePlayers)
* Add unregister command
* Change updater message format
* Fixed bug where some messages are unintentionally hidden from the log
* Added import command for importing profiles to/from mysql
* Force users to use exactly the same case-sensitive name every time (#85)
* Added password confirmation to register command (#67)
* Added changepassword to admin commands (#104)
* Improved event handling
* Allow other plugins to log users in while they are not registered

# Features
- 6 useful commands to manage your password
- Light, fast and easy to set up
- Secure password storage using industry-standard cryptography
- Protects and hides user's location and inventory
- IP & time-based session continuation
- Straightforward administrative control
- User-friendly captcha system for new players
- Used by thousands of server owners
- Stay secure with automatic update notifications
- Prevents players from getting kicked for being logged in from another location
- 20+ supported languages and more to come

# Installation
```shell script
git clone https://github.com/lenis0012/LoginSecurity-2.git LoginSecurity
cd LoginSecurity
git submodule init
git submodule update
mcn clean install
```

Update changes in the translations repo using `git submodule update --remote src/main/resources/lang`
