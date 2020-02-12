A small web app which allows me to quickly access the servers in my private network via SSH.

This is hacked and not tested at all. Please don't use it. ;)

![Screenshot](https://i.imgur.com/TgJxR3g.png)

# Connection strings (examples)

The base64 password hash (0relocFXy/kW5nBaQi3Thtf9OTTE8JWmzSvM7Swl8H0=) you can find in the sources is RHF-e425-XpP.

```
// if no password is set, it tries to authenticate via publickey
ws://ssh:RHF-e425-XpP@localhost:8080/ssh/nuc@nuc
ws://ssh:RHF-e425-XpP@localhost:8080/ssh/nuc@nuc{"command":"ls -l"}

ws://ssh:RHF-e425-XpP@localhost:8080/ssh/nuc@nuc{"password":"abc"}
```

- [xterm.js](https://xtermjs.org/)
- [ktor (Kotlin)](https://ktor.io/)
- WebSockets
- [vue.js](https://vuejs.org/)
- [Klaxon (JSON parser)](https://github.com/cbeust/klaxon)
- [sshj](https://github.com/hierynomus/sshj)
