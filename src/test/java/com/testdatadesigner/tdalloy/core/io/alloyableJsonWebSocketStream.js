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
//var filePathOut = path.join(__dirname, "test_out.txt");
////var filePathIn = path.join(__dirname, "test_in.txt");
//var filePathIn = path.join(__dirname, "test_in.json");

//var ws = fs.createWriteStream(filePathOut);
var ws = new WebSocket('ws://localhost:8080/events/');
//var rs = fs.createReadStream(filePathIn);
////var rs = JSONStream.parse(alloyable)
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

// http://stackoverflow.com/questions/18729405/how-to-convert-utf8-string-to-byte-array
// https://github.com/google/closure-library/blob/e877b1eac410c0d842bcda118689759512e0e26f/closure/goog/crypt/crypt.js
function utf16to8ByteArray(str) {
  var out = [], p = 0;
  for (var i = 0; i < str.length; i++) {
    var c = str.charCodeAt(i);
    if (c < 128) {
      out[p++] = c;
    } else if (c < 2048) {
      out[p++] = (c >> 6) | 192;
      out[p++] = (c & 63) | 128;
    } else if (
        ((c & 0xFC00) == 0xD800) && (i + 1) < str.length &&
        ((str.charCodeAt(i + 1) & 0xFC00) == 0xDC00)) {
      // Surrogate Pair
      c = 0x10000 + ((c & 0x03FF) << 10) + (str.charCodeAt(++i) & 0x03FF);
      out[p++] = (c >> 18) | 240;
      out[p++] = ((c >> 12) & 63) | 128;
      out[p++] = ((c >> 6) & 63) | 128;
      out[p++] = (c & 63) | 128;
    } else {
      out[p++] = (c >> 12) | 224;
      out[p++] = ((c >> 6) & 63) | 128;
      out[p++] = (c & 63) | 128;
    }
  }
  return out;
}

rs.push(new Buffer(utf16to8ByteArray(alloyable)))
rs.push(null)
rs.pipe(ws)
//ws.end();

