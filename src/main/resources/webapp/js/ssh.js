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
            if (event.ctrlKey && !event.altKey && !event.shiftKey) {
                var a = 'A'.charCodeAt(0);
                var z = 'Z'.charCodeAt(0);

                if (event.keyCode >= a
                    && event.keyCode <= z) {
                    this.send(1 + (event.keyCode - a));
                }
            }
        },
        preventShortcuts: function (event) {
            if (event.ctrlKey && !event.altKey && !event.shiftKey) {
                event.preventDefault();
            }
        },
        send: function (keyCode) {
            if (this.socket != null) {
                this.socket.send(String.fromCharCode(keyCode))
            }
        }
    }
});
