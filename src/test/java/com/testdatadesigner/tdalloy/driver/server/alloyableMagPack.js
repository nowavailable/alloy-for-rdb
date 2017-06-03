/**
 * $ mvn clean compile package
 * $ nvm use 5.11.0
 * $ npm install java
 * $ npm install msgpack5
 * $ node src/test/java/com/testdatadesigner/tdalloy/core/io/alloyableMagPack.js
 */
var java = require("../../../../../../../../node_modules/java");

var msgpack = require("../../../../../../../../node_modules/msgpack5")()
var encode  = msgpack.encode
var decode  = msgpack.decode

function MyType(size, value) {
  this.value = value
  this.size  = size
}

function mytipeEncode(obj) {
  var buf = new Buffer(obj.size)
  buf.fill(obj.value)
  return buf
}

function mytipeDecode(data) {
  var result = new MyType(data.length, data.toString('utf8', 0, 1))
  var i;

  for (i = 0; i < data.length; i++) {
    if (data.readUInt8(0) != data.readUInt8(i)) {
      throw new Error('should all be the same')
    }
  }

  return result
}

var a       = new MyType(2, 'a')
var encode  = msgpack.encode
var decode  = msgpack.decode

msgpack.register(0x42, MyType, mytipeEncode, mytipeDecode)

console.log(decode(encode(a)) instanceof MyType)
// true
console.log(decode(encode(a)))
// { value: 'a', size: 2 }