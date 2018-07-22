var vm = new Vue({
    el: '#app',
    data: {
        connectionString: 'ws://localhost:8080/ssh/nuc@nuc',
        socket: null,
        term: null
    },
    mounted: function () {
        Terminal.applyAddon(fit);
        Terminal.applyAddon(attach);
        this.term = new Terminal();
        this.term.open(document.getElementById('terminal'));
        this.term.fit();
    },
    methods: {
        connect: function () {
            if (this.socket != null) {
                this.socket.onclose = function (event) {
                    vm.createSocket();
                };
                this.socket.close();
            } else {
                this.createSocket();
            }
        },
        createSocket: function () {
            var socket = new WebSocket(this.connectionString);
            this.term.reset();
            this.term.attach(socket);
            // TODO: error handling ERR_CONNECTION_REFUSED
            socket.onopen = function (event) {
            };
            socket.onclose = function (event) {
                vm.socket = null;
            };
            socket.onerror = function (event) {
                vm.term.write('Error while connecting to ' + vm.connectionString);
            };
            this.socket = socket;
        }
    }
});
