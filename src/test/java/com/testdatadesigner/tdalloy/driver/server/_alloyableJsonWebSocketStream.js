/**
 * $ mvn clean compile package
 * $ nvm use 5.11.0 
 * $ npm install java  
 * $ npm install websocket-stream
 * $ npm install JSONStream 
 * $ java -jar target/tdalloy-0.1.0-SNAPSHOT-jar-with-dependencies.jar 
 * $ node src/test/java/com/testdatadesigner/tdalloy/core/io/alloyableWebSocketStream.js
 */
var JSONStream = require('../../../../../../../../node_modules/JSONStream');
var java = require("../../../../../../../../node_modules/java");
java.classpath.push("./target/tdalloy-0.1.0-SNAPSHOT-jar-with-dependencies.jar");
var imp = java.newInstanceSync("com.testdatadesigner.tdalloy.core.io.Importer");
java.callStaticMethodSync("com.testdatadesigner.tdalloy.igniter.Bootstrap","setProps")
//var alloyable = JSON.parse(java.callMethodSync(imp, "getAlloyableJSON","./src/test/resources/naming_rule.dump", "mysql"));

var WebSocket = require('../../../../../../../../node_modules/websocket-stream');

var fs = require('fs'),
    path = require('path');
var filePathOut = path.join(__dirname, "test.txt");
var filePathIn = path.join(__dirname, "test_in.txt");

//var ws = fs.createWriteStream(filePathOut);
var ws = new WebSocket('ws://localhost:8080/events/');
var rs = fs.createReadStream(filePathIn);
//var rs = JSONStream.parse(java.callMethodSync(imp, "getAlloyableJSON","./src/test/resources/naming_rule.dump", "mysql"))

ws.on('pipe', function() {
  console.log("Piping");
}).on('error', function(err) {
  console.log(err);
  ws.end();
  //ws.socket.terminate();
}).on('close', function(err) {
  console.log("Closing");
  //ws.socket.terminate();
});

rs.pipe(ws)
//ws.end();
