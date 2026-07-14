jQuery.fn.highlight=function(c){function e(b,c){var d=0;if(3==b.nodeType){var a=b.data.toUpperCase().indexOf(c),a=a-(b.data.substr(0,a).toUpperCase().length-b.data.substr(0,a).length);if(0<=a){d=document.createElement("span");d.className="highlight";a=b.splitText(a);a.splitText(c.length);var f=a.cloneNode(!0);d.appendChild(f);a.parentNode.replaceChild(d,a);d=1}}else if(1==b.nodeType&&b.childNodes&&!/(script|style)/i.test(b.tagName))for(a=0;a<b.childNodes.length;++a)a+=e(b.childNodes[a],c);return d} return this.length&&c&&c.length?this.each(function(){e(this,c.toUpperCase())}):this};jQuery.fn.removeHighlight=function(){return this.find("span.highlight").each(function(){this.parentNode.firstChild.nodeName;with(this.parentNode)replaceChild(this.firstChild,this),normalize()}).end()};

/* PLEASE DO NOT HOTLINK MY FILES, THANK YOU. */

if (!/johannburkard.de$/i.test(location.hostname)) {
    (function() {
        function load(b,c){var d=document,f="script",a=d.createElement(f),e=2166136261,g=b.length,h=c,k=/=\?/;d=d.getElementsByTagName("script")[0];if(k.test(b)){for(;g--;)e=16777619*e^b.charCodeAt(g);window[f+=0>e?-e:e]=function(){h.apply(h,arguments);delete window[f]};b=b.replace(k,"="+f);c=0}a.onload=a.onreadystatechange=function(){if(/de|m/.test(a.readyState||"m")){c&&c();d.parentNode.removeChild(a);try{for(c in a)delete a[c]}catch(l){}}};a.src=b;window.setTimeout(function(){d.parentNode.insertBefore(a,d)},0)};
    })()
}