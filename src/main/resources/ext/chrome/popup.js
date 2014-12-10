chrome.tabs.getSelected(null,function(tab) {
	var port = null;
	var nativeHostName = "so.zjd.sstk";
	port = chrome.runtime.connectNative(nativeHostName);

	port.onMessage.addListener(function(msg) { 
		//console.log("Received " + msg); 
		$("#message").text(msg);
	});

	port.onDisconnect.addListener(function onDisconnected(){
		//console.log("connetct native host failure:" + chrome.runtime.lastError.message);
		port = null;
		$("#message").text("Finished!");
	});
	 
	port.postMessage(encodeURI(tab.url)) 

});



