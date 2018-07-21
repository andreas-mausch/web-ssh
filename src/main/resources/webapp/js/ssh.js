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
            this.socket = new WebSocket(this.connectionString);
            this.term.attach(this.socket);
            // TODO: error handling ERR_CONNECTION_REFUSED
            this.socket.onopen = function (event) {
                vm.term.clear();
            };
            this.socket.onclose = function (event) {
                vm.socket = null;
            };
        }
    }
});
