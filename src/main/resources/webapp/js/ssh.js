Terminal.applyAddon(fit);
Terminal.applyAddon(attach);

class Session {
    constructor(connectionString) {
        this.connectionString = connectionString;
        this.socket = null;
    }

    connect() {
        if (this.connected()) {
            this.socket.onclose = function (event) {
                this.createSocket();
            };
            this.socket.close();
        } else {
            this.createSocket();
        }
    }

    connected() {
        return this.socket != null;
    }

    createSocket() {
        const _this = this;
        _this.socket = new WebSocket(this.connectionString);
        // TODO: error handling ERR_CONNECTION_REFUSED
        _this.socket.onopen = function (event) {
        };
        _this.socket.onclose = function (event) {
            _this.socket = null;
        };
        _this.socket.onerror = function (event) {
            // TODO: how?
            // vm.term.write('Error while connecting to ' + vm.connectionString);
        };
    }
}

Vue.component('ssh-session', {
    props: {
        'session': Session
    },
    data: function () {
        return {
            term: null
        }
    },
    template:
    '<div class="row ssh-session">' +
    '   <i class="fas fa-3x fa-plug"' +
    '       v-bind:style="{ color: session.connected() ? \'green\' : \'gray\' }">' +
    '   </i>' +
    '   <div class="term">' +
    '   </div>' +
    '</div>',
    mounted: function () {
        Terminal.applyAddon(fit);
        Terminal.applyAddon(attach);
        this.term = new Terminal({
            theme: {
                background: '#303030'
            }
        });
        this.term.open(this.$el.querySelector(".term"));
        this.term.fit();

        this.term.attach(this.session.socket);
    }
});

new Vue({
    el: '#app',
    data: {
        connectionString: 'ws://localhost:8080/ssh/nuc@nuc',
        sessions: [],
        currentSession: null
    },
    methods: {
        connect: function () {
            let session = new Session(this.connectionString);
            session.connect();
            this.currentSession = session;
            this.sessions.push(session)
        }
    }
});
