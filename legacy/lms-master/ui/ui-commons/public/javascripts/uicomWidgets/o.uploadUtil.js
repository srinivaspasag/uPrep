var uploadUtil=new(function($){
    var blobs = [];
        
    function uploadChunk(blob, fileName, fileType){
        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/application/upload', true);
        xhr.onload = function(e){
            document.getElementById("messages").innerHTML += "Chunk of size " + blob.size + " uploaded successfully.<br/>";
        }
        xhr.setRequestHeader('x-file-name', fileName);        
        xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        xhr.setRequestHeader('Content-Type', fileType)
        document.getElementById("messages").innerHTML += "Uploading chunk of size " + blob.size + ".<br/>";
        xhr.send(blob);
    }
    /*
    * Invoke this function when the file is selected.
    */
    document.querySelector('#userfile').addEventListener('change', function(){
        var file = this.files[0];
        var bytes_per_chunk = 512 * 1024;
        var start = 0;
        var end = bytes_per_chunk;
        var size = file.size;
        while (start < size) {
        //push the fragments to an array
        blobs.push(file.slice(start, end));
        start = end;
        end = start + bytes_per_chunk;
        }
        //uploadChunk(file,file.name,file.type)
        //upload the fragment to the server
        while (blob = blobs.shift()) {
            uploadChunk(blob, file.name, file.type);
        }
    })
    
})();