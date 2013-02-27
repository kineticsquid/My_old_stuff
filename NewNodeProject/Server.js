var http = require("http");
var url = require("url");
var diagnostics = require("./Diagnostics");

function start(route) {

  function onRequest(request, response) {
    response.writeHead(200, {"Content-Type": "text/plain"});
    response.write("Hello World again\n");
    var pathname = url.parse(request.url).pathname;
    route(pathname);
    diagnostics.output(request, response);
    response.end();
  }

  http.createServer(onRequest).listen(8888);
  console.log("Server has started.");
}

exports.start = start;