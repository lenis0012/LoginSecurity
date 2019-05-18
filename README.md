LoginSecurity 3.0 [![Build Status](https://ci.codemc.org/job/lenis0012/job/LoginSecurity/badge/icon)](https://ci.codemc.org/job/lenis0012/job/LoginSecurity/)
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
- Login with custom password
- Hidden pre-login location
- Hidden inventory
- Secure password storage with BCrypt.
- Captcha
- Sessions & Login timeout
- Username filter
- UUID Support
- Advanced and detailed configuration
- Action-based system & API
- Reliable and fast
- Easy to set up
- Used by thousands of server owners
- 20 existing translations and more to come!
