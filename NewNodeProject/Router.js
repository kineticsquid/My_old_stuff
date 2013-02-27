var log = require ("./log");
var requesstHandlers = require ("./requestHandlers");

function route(pathname) {
  log.log("About to route a request for " + pathname);
}

exports.route = route;