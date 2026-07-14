var count = 0;
function formatBytes(a,b){if(0==a)return"0 Bytes";var c=1024,d=b||2,e=["Bytes","KB","MB","GB","TB","PB","EB","ZB","YB"],f=Math.floor(Math.log(a)/Math.log(c));return parseFloat((a/Math.pow(c,f)).toFixed(d))+" "+e[f]}

print("Cmds Video Id"+","+"Name"+","+"UUID"+","+"Original size"+","+"Encrypted size"+","+"Converted size"+","+"TotalSize")
db.cmdsvideos.find({"contentSrc.id":"5cfdfd19e4b03689d83132ff",linkType:"UPLOADED"}).forEach(function(doc){
    if(doc.size.initialized != false){
        count++;
        print(doc._id.valueOf()+","+"\""+doc.name+"\""+","+doc.uuid+","+formatBytes(doc.size.original)+","+formatBytes(doc.size.encrypted)+","+formatBytes(doc.size.converted)+","+formatBytes(doc.size.totalSize));
    }
});
print("Total Count:"+count);

