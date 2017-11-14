chrome.webRequest.onBeforeRequest.addListener(
    function(info) {
        chrome.tabs.query({active: true, currentWindow: true}, function(tabs){
            chrome.tabs.sendMessage(tabs[0].id, {
                url: info.url,
                body: info.requestBody.formData
            }, function(response) {});  
        });
        return {cancel: false};
    },
    // filters
    {
        urls: [
            "https://*.messenger.com/ajax/*", "https://*.facebook.com/ajax/*"
        ],
    },
    // extraInfoSpec
    ["blocking", "requestBody"]);
