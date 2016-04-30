/**
 * $ mvn clean compile package
 * $ nvm use 5.11.0 
 * $ npm install java  
 * $ npm install websocket-stream
 * $ java -jar target/tdalloy-0.1.0-SNAPSHOT-jar-with-dependencies.jar 
 * $ node src/test/java/com/testdatadesigner/tdalloy/core/io/alloyableWebSocketStream.js
 */
var stream = require('stream')
//  , util = require('util')
  ;

//var JSONStream = require('../../../../../../../../node_modules/JSONStream');
var java = require("../../../../../../../../node_modules/java");
java.classpath.push("./target/tdalloy-0.1.0-SNAPSHOT-jar-with-dependencies.jar");
var imp = java.newInstanceSync("com.testdatadesigner.tdalloy.core.io.Importer");
java.callStaticMethodSync("com.testdatadesigner.tdalloy.igniter.Bootstrap","setProps")
//var alloyable = JSON.parse(java.callMethodSync(imp, "getAlloyableJSON","./src/test/resources/naming_rule.dump", "mysql"));
var alloyable = java.callMethodSync(imp, "getAlloyableJSON","./src/test/resources/naming_rule.dump", "mysql")

var WebSocket = require('../../../../../../../../node_modules/websocket-stream');

var fs = require('fs'),
    path = require('path');
var filePathOut = path.join(__dirname, "test_out.txt");
var filePathIn = path.join(__dirname, "test_in.txt");

//var ws = fs.createWriteStream(filePathOut);
var ws = new WebSocket('ws://localhost:8080/events/');
//var rs = fs.createReadStream(filePathIn);
//var rs = JSONStream.parse(alloyable)
var rs = new stream.Readable({ objectMode: true });

rs.on('data', function(data){
  console.log("Read ok.");
  console.log(data.length)
//  if (ws.write(data) === false) {
//	  console.log("Read paused.")
//	  rs.pause()
//  }
}).on('end', function(){
  console.log('Read ended.')
}).on('close', function(){
  console.log('Read closed.')
}).on('error', function(err) {
  console.log("Read error: " + err);
  ws.end();
})

ws.on('pipe', function() {
  console.log("Piping");
}).on('error', function(err) {
  console.log("Write error: " + err);
  ws.end();
  ws.socket.terminate();
}).on('close', function(err) {
  console.log("Closing");
  ws.socket.terminate();
});

rs.push(alloyable)
rs.push(null)
rs.pipe(ws)
//ws.end();
