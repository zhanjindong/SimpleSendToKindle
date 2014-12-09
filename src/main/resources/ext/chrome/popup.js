var port = null;
var nativeHostName = "so.zjd.sstk";
port = chrome.runtime.connectNative(nativeHostName);

port.onMessage.addListener(function(msg) { 
	alert("Received " + msg);
    console.log("Received " + msg); 
});

port.onDisconnect.addListener(function onDisconnected(){
	//alert("connetct native host failure:" + chrome.runtime.lastError.message);
	port = null;
});
 
port.postMessage(encodeURI("http://www.cnblogs.com/michaelxu/archive/2007/05/14/745881.html")) 


