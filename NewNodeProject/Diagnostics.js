var url = require("url");
var log = require ("./log");

function output(request, response) { 
  log.log("path: " + url.parse(request.url).path);
  log.log("href: " + url.parse(request.url).href);
  log.log("protocol: " + url.parse(request.url).protocol);
  log.log("host: " + url.parse(request.url).host);
  log.log("auth: " + url.parse(request.url).auth);
  log.log("hostname: " + url.parse(request.url).hostname);
  log.log("port: " + url.parse(request.url).port);
  log.log("search: " + url.parse(request.url).search);
  log.log("path: " + url.parse(request.url).path);
  log.log("query: " + url.parse(request.url).query);
  log.log("hash: " + url.parse(request.url).hash);
    
}

exports.output = output;