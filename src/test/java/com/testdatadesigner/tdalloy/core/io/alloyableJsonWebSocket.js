/**
 * $ mvn clean compile package
 * $ nvm use 5.11.0
 * $ npm install java
 * $ npm install ws
 * $ java -jar target/tdalloy-0.1.0-SNAPSHOT-jar-with-dependencies.jar 
 * $ node src/test/java/com/testdatadesigner/tdalloy/core/io/alloyableWebSocket.js
 */
var java = require("../../../../../../../../node_modules/java");
java.classpath.push("./target/tdalloy-0.1.0-SNAPSHOT-jar-with-dependencies.jar");
var imp = java.newInstanceSync("com.testdatadesigner.tdalloy.core.io.Importer");
java.callStaticMethodSync("com.testdatadesigner.tdalloy.igniter.Bootstrap", "setProps")
var alloyable = JSON.parse(java.callMethodSync(imp, "getAlloyableJSON", "./src/test/resources/naming_rule.dump", "mysql"));

var WebSocket = require('ws');
var ws = new WebSocket('ws://localhost:8080/events/');
ws.on('open', function open() {
	ws.send(JSON.stringify(alloyable))
	ws.terminate();
})