/**
 * $ mvn clean compile package
 * $ nvm use 5.11.0
 * $ npm install java
 * $ node src/test/java/com/testdatadesigner/tdalloy/core/io/alloyableJson.js
 */
var java = require("../../../../../../../../node_modules/java");
java.classpath.push("./target/tdalloy-0.1.0-SNAPSHOT-jar-with-dependencies.jar");
var imp = java.newInstanceSync("com.testdatadesigner.tdalloy.core.io.Importer");
java.callStaticMethodSync("com.testdatadesigner.tdalloy.igniter.Bootstrap", "setProps")
var alloyable = JSON.parse(java.callMethodSync(imp, "getAlloyableJSON", "./src/test/resources/naming_rule.dump", "mysql"));
console.log(alloyable);
