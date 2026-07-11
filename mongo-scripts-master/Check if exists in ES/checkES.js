function httpGet()
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", "localhost:9200/tests/_search?q=id:5722f2ba44ae389bea4c541e&_source=false&size=0", false );
    print(xmlHttp.responseText);
}
httpGet();