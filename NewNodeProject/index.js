var server = require("./server");
var router = require("./Router");

server.start(router.route);