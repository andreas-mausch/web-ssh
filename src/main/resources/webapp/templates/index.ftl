<html>
<head>
    <link rel="stylesheet" href="static/main.css"/>
    <link rel="stylesheet" href="https://unpkg.com/xterm@3.5.1/dist/xterm.css"/>
    <link rel="stylesheet" href="https://unpkg.com/@fortawesome/fontawesome-free@5.1.1/css/all.css"/>
    <title>Web-SSH</title>
</head>

<div id="app">
    <div class="row">
        <input v-model="connectionString">
        <button v-on:click="connect">Connect</button>
        <i class="fas fa-3x fa-plug"
           v-bind:style="{ color: socket != null ? 'green' : 'gray' }">
        </i>
    </div>

    <div id="terminal" class="row">
    </div>
</div>

<script src="https://unpkg.com/vue@2.5.16"></script>
<script src="https://unpkg.com/xterm@3.5.1/dist/xterm.js"></script>
<script src="https://unpkg.com/xterm@3.5.1/dist/addons/fit/fit.js"></script>
<script src="https://unpkg.com/xterm@3.5.1/dist/addons/attach/attach.js"></script>
<script src="static/ssh.js"></script>
</html>
