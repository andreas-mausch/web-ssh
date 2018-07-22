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
            if (_this.onerror != null) {
                _this.onerror('Error while connecting to ' + _this.connectionString);
            }
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
    '<div v-show="isCurrent()" class="row ssh-session">' +
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

        const _this = this;
        this.session.onerror = function (message) {
            _this.term.write(message);
        };
        this.term.attach(this.session.socket);
        this.fit();
    },
    updated: function () {
        this.fit();
    },
    methods: {
        isCurrent: function () {
            return this.session === vm.currentSession;
        },
        fit: function () {
            const _this = this;
            _this.$nextTick(function () {
                _this.term.fit();
                this.term.focus();
            });
        }
    }
});

const vm = new Vue({
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
        },
        displayString: function (session) {
            return session.connectionString.split("/").pop();
        }
    }
});
