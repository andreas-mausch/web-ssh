<html>
<head>
    <link href="static/main.css" rel="stylesheet"/>
    <title>Web-SSH</title>
</head>

<div id="app">
    <input v-model="connectionString">
    <button v-on:click="connect">Connect</button>

    <textarea v-model="consoleOutput"></textarea>
</div>

<script src="https://unpkg.com/vue@2.5.16"></script>
<script src="static/ssh.js"></script>
</html>
