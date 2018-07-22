<html>
<head>
    <link rel="stylesheet" href="static/main.css"/>
    <link rel="stylesheet" href="https://unpkg.com/xterm@3.5.1/dist/xterm.css"/>
    <link rel="stylesheet" href="https://unpkg.com/@fortawesome/fontawesome-free@5.1.1/css/all.css"/>
    <link href='https://fonts.googleapis.com/css?family=Montserrat:700' rel='stylesheet' type='text/css'>
    <title>Web-SSH</title>
</head>

<div id="app" v-cloak>
    <div>
        <div class="connection-bar">
            <input class="connection-string" v-model="connectionString">
            <button class="wiggly-button" v-on:click.prevent="connect">Connect</button>
        </div>
        <div class="favorites">
            <ul>
                <li v-for="(url, name) in favorites" class="favorite">
                    <a href="#"
                       :data-url="url"
                       v-on:click.prevent="connectToFavorite(name)">
                        <span>{{ name }}</span>
                    </a>
                </li>
            </ul>
        </div>

        <div class="tabs">
            <ul>
                <li v-for="session in sessions"
                    v-bind:class="{ active: currentSession == session }">
                    <a href="#"
                       v-on:click.prevent="currentSession = session">
                        <span>{{ displayString(session) }}</span>
                    </a>
                    <i class="fas fa-2x fa-plug"
                       v-bind:style="{ color: session.connected() ? 'green' : 'gray' }">
                    </i>
                </li>
            </ul>
        </div>
    </div>

    <ssh-session
            v-for="session in sessions"
            v-bind:session="session">
    </ssh-session>
</div>

<script src="https://unpkg.com/vue@2.5.16"></script>
<script src="https://unpkg.com/xterm@3.5.1/dist/xterm.js"></script>
<script src="https://unpkg.com/xterm@3.5.1/dist/addons/fit/fit.js"></script>
<script src="https://unpkg.com/xterm@3.5.1/dist/addons/attach/attach.js"></script>
<script src="static/ssh.js"></script>
</html>
