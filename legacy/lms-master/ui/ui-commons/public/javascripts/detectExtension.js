function checkBrowser(){
    var result = bowser.getParser(window.navigator.userAgent);
    var browser;
    try{
        browser = result.parsedResult.browser.name;
    }
    catch(err){
        browser = "N.A";
        console.log(err);
    }
    return browser;
}
var browser = checkBrowser();
console.log(browser);


if(browser == "Chrome" || browser == "Firefox" || browser == "Microsoft Edge"){
    $.when(detect('chrome-extension://ngpampappnmepgilojfohadhhmbhlaek/captured.js'),detect('moz-extension://52ef0d61-0427-4a89-b401-e01c0033a2ff/captured.js'),detect('ms-browser-extension://EdgeExtension_TonecIncIDMIntegrationModule_e7b5mm5d3r6v2/captured.js')).then(function(){
        console.log("All 3 requests done");
    });
}
else if(browser == "Safari"){
    $(".videoJsPlayer").removeClass("nonner");
    try{
        $("#html5VideoTag video")[0].load();
    }
    catch(err){
        console.log(err);
        console.log(browser);
    }
}
else{
    $(".videoJsPlayer").html("");
    $(".videoPlayerHolder").html("<div class='whiteTextColor relative'><div class='warningDiv'>"+
        "<div class='title'>We do not support this browser to play videos.</div>"+
        "<div class='warningHead'>Please use latest Chrome/Firefox/Edge browsers.</div></div>");
}

function detect(url){
    $.ajax({
        url:url,
        type:"HEAD",
        success:function(data){
            $(".videoJsPlayer").html("");
            $(".videoPlayerHolder").html("<div class='whiteTextColor warningContainer relative'><div class='warningDiv'>"+
                "<div class='title'>video downloader extension detected.</div>"+
                "<div class='warningHead'>Please disable the extension and reload the page.</div>"+
                "<div class='warningSubHead'>For more information:  <a href='https://www.computerhope.com/issues/ch001411.htm' target=_blank>Click here</a></div>"+
                "</div></div>");
        },
        error:function(data){
            $(".videoJsPlayer").removeClass("nonner");
            try{
                $("#html5VideoTag video")[0].load();
            }
            catch(err){
                console.log(err);
            }
        }
    })
}