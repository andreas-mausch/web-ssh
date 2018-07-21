<html>
<head>
    <link href="static/main.css" rel="stylesheet"/>
    <title>Web-SSH</title>
</head>

<div id="app">
    <input v-model="connectionString">
    <button v-on:click="connect">Connect</button>

    <div>
        <textarea v-model="consoleOutput"
                  @keydown="preventShortcuts"
                  @keypress.prevent="keyPressed"
                  @keyup.prevent="keyUp"
                  class="consoleOutput"></textarea>
    </div>
</div>

<script src="https://unpkg.com/vue@2.5.16"></script>
<script src="static/ssh.js"></script>
</html>
