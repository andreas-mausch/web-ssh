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
            this.socket.onopen = function (event) {
                vm.socket.send('Hallo');
            };
            this.socket.onclose = function (event) {
            };
            this.socket.onmessage = function (event) {
                vm.consoleOutput += event.data;
            };
            this.socket.onerror = function (event) {
            };
        }
    }
});
