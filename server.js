var express = require('express');
var server = express.createServer();
server.configure(function(){
  server.use(express.static(__dirname));
});
server.listen(8002);
