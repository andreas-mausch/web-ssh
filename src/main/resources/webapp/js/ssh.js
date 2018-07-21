var vm = new Vue({
    el: '#app',
    data: {
        connectionString: 'ws://localhost:8080/ssh/nuc@nuc',
        socket: null,
        consoleOutput: ''
    },
    methods: {
        connect: function () {
            this.consoleOutput = '';

            this.socket = new WebSocket(this.connectionString);
            // TODO: error handling ERR_CONNECTION_REFUSED
            this.socket.onopen = function (event) {
            };
            this.socket.onclose = function (event) {
                vm.socket = null;
            };
            this.socket.onmessage = function (event) {
                for (var i = 0; i < event.data.length; i++) {
                    if (event.data.charCodeAt(i) === 8) {
                        vm.consoleOutput = vm.consoleOutput.slice(0, -1);
                    }
                    else if (event.data.charCodeAt(i) === 27
                        && i + 2 < event.data.length
                        && event.data.charAt(i + 1) === '[') {
                        i += 2;
                        // TODO handle special characters, cursor positioning and colors
                    }
                    else {
                        vm.consoleOutput += event.data.charAt(i);
                    }
                }
            };
            this.socket.onerror = function (event) {
            };
        },
        keyPressed: function (event) {
            this.send(event.which);
        },
        keyUp: function (event) {
            var specialKey = this.handleSpecialKey(event);
            if (specialKey !== null) {
                this.send(specialKey);
            }
        },
        preventShortcuts: function (event) {
            if (this.handleSpecialKey(event) !== null) {
                event.preventDefault();
            }
        },
        handleSpecialKey: function (event) {
            if (!this.connected()) {
                return null;
            }
            if (event.ctrlKey && !event.altKey && !event.shiftKey) {
                var a = 'A'.charCodeAt(0);
                var z = 'Z'.charCodeAt(0);

                if (event.keyCode >= a
                    && event.keyCode <= z) {
                    return 1 + (event.keyCode - a);
                }
            }
            if (event.which === 8) {
                return event.which;
            }
            return null;
        },
        onPaste: function (event) {
            this.sendString(event.clipboardData.getData('Text'));
        },
        connected: function () {
            return this.socket != null;
        },
        send: function (keyCode) {
            this.sendString(String.fromCharCode(keyCode));
        },
        sendString: function (string) {
            if (this.connected()) {
                this.socket.send(string);
            }
        }
    }
});
