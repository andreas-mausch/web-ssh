var vm = new Vue({
    el: '#app',
    data: {
        connectionString: 'ws://localhost:8080/ssh/nuc@nuc',
        socket: null,
        term: null
    },
    mounted: function () {
        Terminal.applyAddon(fit);
        this.term = new Terminal();
        this.term.open(document.getElementById('terminal'));
        this.term.fit();
        this.term.on('data', function (data) {
            vm.send(data);
        });
    },
    methods: {
        connect: function () {
            this.socket = new WebSocket(this.connectionString);
            // TODO: error handling ERR_CONNECTION_REFUSED
            this.socket.onopen = function (event) {
                vm.term.clear();
            };
            this.socket.onclose = function (event) {
                vm.socket = null;
            };
            this.socket.onmessage = function (event) {
                vm.term.write(event.data);
            };
            this.socket.onerror = function (event) {
            };
        },
        connected: function () {
            return this.socket != null;
        },
        send: function (string) {
            if (this.connected()) {
                this.socket.send(string);
            }
        }
    }
});
