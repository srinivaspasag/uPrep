/*Imp things about the grapher
 *
 *A list of yaxis items can be passed(but be careful, the last item in the list would be treated as ymax and the list length as
 *y axis item num). This list is given the highest preference.No such list is currently available for x-axis.
 *
 * In case of area,line and bar graph the data format is
 * {ajith:[{itemName:'a',itemValue:'30',listData:["Rank:20/320","Marks:30/90"]},
 * {itemName:'b',itemValue:'20',listData:["Rank:20/320","Marks:30/90"]},
 * {itemName:'c',itemValue:'47',listData:["Rank:20/320","Marks:30/90"]},
 * {itemName:'d',itemValue:'90',listData:["Rank:20/320","Marks:30/90"]}],
 * anil:[{itemName:'a',itemValue:'50',listData:["Rank:20/320","Marks:30/90"]},
 * {itemName:'b',itemValue:'46',listData:["Rank:20/320","Marks:30/90"]},
 * {itemName:'c',itemValue:'50',listData:["Rank:20/320","Marks:30/90"]},
 *{itemName:'d',itemValue:'67',listData:["Rank:20/320","Marks:30/90"]}]}
 *
 *
 *In case of pir chart the data format is.
 *[{name:"physics",questions:15},{name:"chemostry",questions:5},{name:"MatheMatics",questions:3}
   ,{name:"economics",questions:13},{name:"History & Civics",questions:9},{name:"Social Studies",questions:5}]
 *
 **/



$.fn.createGraph = function(graphType,graphDetails,graphDimen,dataObj) {
    $(this).html(createGraphMap[graphType](graphDetails,graphDimen,dataObj));
};
var createGraphMap=new Object();
createGraphMap["BAR"]=function(graphDetails,graphDimen,dataObj){
    return barLineGraphFn.drawGraph(graphDetails,graphDimen,dataObj,"BAR");
}
createGraphMap["AREA"]=function(graphDetails,graphDimen,dataObj){
    return barLineGraphFn.drawGraph(graphDetails,graphDimen,dataObj,"AREA");
}
createGraphMap["PIE"]=function(graphDetails,graphDimen,dataObj){
    return pieChartFn.drawGraph(graphDetails,graphDimen,dataObj);
}
var graphGlobalFns={
    //fixed
    ledgerColors:["#00cc00","#0099cc","#0055cc","#8800cc","#d9007e","#ff0000","#ff9900","#ffcc00","#ffff00","#ace600"],
    areaColors:["#fafab5","#bee9af","#afe9e4","#bfcdf1"],
    areaLineColors:["#b9b889","#90b683","#74aaa5","#808aa3"],
    //comming from user
    totalWidth:700,
    totalHeight:450,
    graphTitle:"",
    makeSVG:function(tag, attrs) {
        var el= document.createElementNS("http://www.w3.org/2000/svg", tag);
        for (var k in attrs)
            el.setAttribute(k, attrs[k]);
        return $(el);
    },
    makeTextSVG:function(attrs,textContent,dy,breakTextNum) {
        var el= document.createElementNS("http://www.w3.org/2000/svg", 'text');
        
        if(textContent!=undefined&& textContent.length!=undefined&&textContent.length>breakTextNum&&breakTextNum!=0){
          textContent=textContent.substring(0,breakTextNum)+"..";
        }
        var tspan=document.createElementNS("http://www.w3.org/2000/svg","tspan");
        tspan.appendChild(document.createTextNode(textContent));
        tspan.setAttribute("dy", dy);
        el.appendChild(tspan);
        for (var k in attrs)
            el.setAttribute(k, attrs[k]);
        return $(el);
    },
    makeSVGLink:function(attrs,textAttrs,textContent){
        var el= document.createElementNS("http://www.w3.org/2000/svg", 'a');
        for (var k in attrs)
            el.setAttribute(k, attrs[k]);
        
        var t=document.createElementNS("http://www.w3.org/2000/svg", 'text');
        t.appendChild(document.createTextNode(textContent));
        for (var i in textAttrs)
            t.setAttribute(i, textAttrs[i]);
        el.appendChild(t);
        var svgWrapper=graphGlobalFns.makeSVG('svg',{"xmlns:xlink":"http://www.w3.org/1999/xlink","xmlns":"http://www.w3.org/1999/xlink"});
        svgWrapper.append(el);
        return svgWrapper;
    },
    createSVGWrapper:function(){
        var SVGContent=this.makeSVG('svg',{style:"overflow: hidden' xmlns='http://www.w3.org/2000/svg' width='"+this.totalWidth,
         version:'1.1',height:this.totalHeight,width:this.totalWidth});
         var backgroundRect=this.makeSVG('rect',{stroke:"none",fill:"rgba(255,255,255,0)",height:this.totalHeight,
         width:this.totalWidth});
         var gTitle=this.makeSVG('g',{style:"font-size: 15px;font-weight: bold;fill:#33bad7;stroke:none;text-anchor:middle",
         'class':'CAHeader'});
         gTitle.append(this.makeTextSVG({y:15,x:this.totalWidth/2},this.graphTitle,15));

         SVGContent.append(backgroundRect).append(gTitle);
         var chartAreaDiv=$(document.createElement('div'));
         chartAreaDiv.attr("class","mainChartArea").attr("style","width:"+this.totalWidth+"px;height:"+this.totalHeight+"px;\n\
    `        font-size:10px;");
         chartAreaDiv.html(SVGContent);
         return chartAreaDiv;
    }
}


var barLineGraphFn={
    //fixed values
    ledgerWidth:80,
    putLedger:true,
    graphAreaGx:0,
    xAxisStrokeWidth:2,
    yAxisStrokeWidth:2,
    lineAreaStrokeWidth:1,

    //comming from User
    graphAreaGy:10,//will be set to 50 or more depending on the title of the graph
    xAxisPad:50,
    yAxisPad:35,
    xAxisItemsNum:4,
    yAxisItemsList:[],//this list if given by user will have preference over item num and ymax, both will be taken from this list
    yAxisItemsNum:5,
    yAxisMax:100,
    ledgerItemsNum:1,
    xAxisTextRotateBy:0,
    breakTextNum:0,
    xAxisTitle:"",
    yAxisTitleWidth:0,
    yAxisTitle:"",
    spaceFromYAxis:0,//this thing is mostly for area,line graphs
    fixedSpacingOfXAxisItems:0,//this thing is mostly for area,line graphs


    //calculated
    xAxisWidth: 490,
    yAxisHeight:300,
    graphAreaWidth:525,
    graphAreaHeight:350,



    drawGraph:function(graphDetails,graphDimen,dataObj,graphType){

            if(graphDetails.title&&graphDetails.title!=""){
                graphGlobalFns.graphTitle=graphDetails.title;
               if(graphDimen.graphAreaGy)this.graphAreaGy=graphDimen.graphAreaGy;
               else this.graphAreaGy=50;
            }
            if(graphDimen.graphAreaGy)this.graphAreaGy=graphDimen.graphAreaGy;

            if(graphDimen.width)graphGlobalFns.totalWidth=graphDimen.width;
            if(graphDimen.height)graphGlobalFns.totalHeight=graphDimen.height;
            if(graphDimen.xAxisPad)this.xAxisPad=graphDimen.xAxisPad;
            if(graphDimen.yAxisPad)this.yAxisPad=graphDimen.yAxisPad;

            if(!graphDetails.putLedger){
             this.putLedger=false;
            }
            
            if(graphDetails.xAxisTitle){
                this.xAxisTitle=graphDetails.xAxisTitle;
            }

            if(graphDetails.yAxisTitle){
                this.yAxisTitle=graphDetails.yAxisTitle;
                this.yAxisTitleWidth=30;
            }

            if(graphDimen.spaceFromYAxis){
                this.spaceFromYAxis=graphDimen.spaceFromYAxis;
            }
            
            if(graphDimen.fixedSpacingOfXAxisItems){
                this.fixedSpacingOfXAxisItems=graphDimen.fixedSpacingOfXAxisItems;
            }
            
            this.graphAreaWidth=graphGlobalFns.totalWidth-this.ledgerWidth;//50 here is kind of margin from right end
            this.graphAreaHeight=graphGlobalFns.totalHeight-this.graphAreaGy;

            this.xAxisWidth= this.graphAreaWidth-this.yAxisPad-this.yAxisTitleWidth;
            this.yAxisHeight=this.graphAreaHeight-this.xAxisPad;

            if(graphDimen.xAxisTextRotateBy)this.xAxisTextRotateBy=graphDimen.xAxisTextRotateBy;
            if(graphDimen.breakTextNum)this.breakTextNum=graphDimen.breakTextNum;

            //finding y axis max and number of items
            if(graphDetails.yAxisItemsList){
                var list=graphDetails.yAxisItemsList;
                this.yAxisItemsList=list;
                this.yAxisMax=list[list.length-1];
                this.yAxisItemsNum=list.length;
            }
            else{
                var yMax=0;
                $.each(dataObj,function(user,stats){
                    $.each(stats,function(item,val){
                        if(val>yMax)yMax=val;
                    });
                });
                if(graphDetails.yAxisMax)this.yAxisMax=graphDetails.yAxisMax;
                if(graphDetails.yAxisItemsNum)this.yAxisItemsNum=graphDetails.yAxisItemsNum;
            }



            //getting the required content

            var ledger="<g></g>";
            if(this.putLedger)ledger=this.getLedger(dataObj);
            var graphAreaRect=this.getGraphAreaRect(graphDimen);
            var xAxis=this.getxAxis(dataObj,graphType);
            var yAxis=this.getyAxis();
            var yAxisTitle=this.getyAxisTitle();
            var xAxisTitle=this.getxAxisTitle();
            var barLines="<g></g>";
            if(graphType=="BAR"){
                barLines=this.getBars(dataObj);
            }
            else if(graphType=="AREA"){
                barLines=this.getAreas(dataObj);
            }


            var gGraphArea=graphGlobalFns.makeSVG('g',{transform:"translate("+this.graphAreaGx+this.yAxisTitleWidth+", "+this.graphAreaGy+")"});
            gGraphArea.append(graphAreaRect).append(xAxis.xAxisPath).append(xAxis.gxAxisItems)
            .append(yAxis.yAxisPath).append(yAxis.gyAxisItems).append(barLines);

            var chartAreaDiv=graphGlobalFns.createSVGWrapper();
            var SVGEl=chartAreaDiv.find("svg");
            this.initializeEvents(SVGEl);
            SVGEl.append(ledger).append(yAxisTitle).append(xAxisTitle).append(gGraphArea).data("dataObj",dataObj);
            return chartAreaDiv;
        },
        getGraphAreaRect:function(){
            var rect=graphGlobalFns.makeSVG('rect',{stroke:"none",fill:"rgba(0,0,0,0)",height:this.graphAreaHeight,
            width:this.graphAreaWidth});
            return rect;
        },
        getxAxis:function(dataObj,graphType){
            var xAxisObj,xAxisItemsList=[];
            $.each(dataObj,function(user,stats){
                xAxisObj=stats;
                return false;
            })
            for(var k=0;k<xAxisObj.length;k++){
                xAxisItemsList.push(xAxisObj[k].itemName);
            }
            this.xAxisItemsNum=xAxisItemsList.length;

            var delta=this.xAxisWidth/(this.xAxisItemsNum+1);
            
            
            if(graphType=="AREA"||graphType=="LINE"){
                delta=this.xAxisWidth/(this.xAxisItemsNum-1);
                if(this.fixedSpacingOfXAxisItems!=0){
                    delta=this.fixedSpacingOfXAxisItems;
                }
            }


            var r=0,textAnchor="middle";
            if(this.xAxisTextRotateBy!=0){
                r=this.xAxisTextRotateBy;
                textAnchor="end";
            }

            var xAxisGx=this.yAxisPad+this.spaceFromYAxis;
            var gxAxisItems=graphGlobalFns.makeSVG('g',{transform:"translate("+xAxisGx+","+(this.yAxisHeight+5)+")",
            'stroke-width': this.xAxisStrokeWidth,'class':"CAX-axisItems",'text-anchor':textAnchor,'stroke':'none'});

            if(graphType=="AREA"||graphType=="LINE"){
                for(var i=0;i<xAxisItemsList.length;i++){
                    gxAxisItems.append(graphGlobalFns.makeTextSVG({x:(delta*i),y:0,transform:"rotate("+r+","+(delta*i)+",0)",
                    title:xAxisItemsList[i],'font-size':11},xAxisItemsList[i],0,this.breakTextNum));
                }
            }
            else{
                for(var n=0;n<xAxisItemsList.length;n++){
                    gxAxisItems.append(graphGlobalFns.makeTextSVG({x:(delta*(n+1)),y:0,transform:"rotate("+r+","+(delta*(n+1))+",0)"}
                    ,xAxisItemsList[n],10,this.breakTextNum));
                }
            }
            var xAxisPath=graphGlobalFns.makeSVG('path',{d:"M"+this.yAxisPad+","+this.yAxisHeight+" \n\
            h"+this.xAxisWidth,'stroke-width':this.xAxisStrokeWidth,stroke:"#666666"});
        
            return {xAxisPath:xAxisPath,gxAxisItems:gxAxisItems};
        },
        getxAxisTitle:function(){            
            var gxAxisTitle=graphGlobalFns.makeSVG('g',{transform:"translate("+(this.graphAreaWidth)+","+(this.yAxisHeight)+")",
            'stroke-width': this.xAxisStrokeWidth,'class':"CAX-axisTitle",'text-anchor':"start",'stroke':'none','font-size':14});
            var xAxisTitle=graphGlobalFns.makeTextSVG({fill:"#33bad7"},this.xAxisTitle,15);
            gxAxisTitle.append(xAxisTitle);
            return gxAxisTitle;
        },
        getyAxis:function(){
            var itemsList=[];
            var y=this.yAxisItemsNum;
            var deltaItems=this.yAxisMax/y;
            if(this.yAxisItemsList.length>0){
                itemsList=this.yAxisItemsList;
            }
            else{
                for(var m=0;m<y;m++){
                    itemsList.push((m+1)*deltaItems);
                }
            }

            var delta=this.yAxisHeight/y;
            var gyAxisItems=graphGlobalFns.makeSVG('g',{transform:"translate("+(this.yAxisPad-10)+","+this.yAxisHeight+")",
            'stroke-width': this.yAxisStrokeWidth,'class':"CAY-axisItems",'text-anchor':'end','stroke':'none'});
            for(var i=0;i<y;i++){
               gyAxisItems.append(graphGlobalFns.makeTextSVG({x:0,y:"-"+((i+1)*delta)},itemsList[i],10));
            }
            var yAxisPath=graphGlobalFns.makeSVG('path',{d:"M"+this.yAxisPad+","+this.yAxisHeight+" \n\
            v-"+this.yAxisHeight,'stroke-width':this.yAxisStrokeWidth,stroke:"#666666"});

            return {yAxisPath:yAxisPath,gyAxisItems:gyAxisItems};
        },
        getyAxisTitle:function(){
                var gyAxisTitle=graphGlobalFns.makeSVG('g',{transform:"translate(15,"+((this.yAxisHeight/2)+this.graphAreaGy)+")",
                'class':"CAY-axisTitle",'text-anchor':'middle','font-size':14,'stroke':'none'});
                var yAxisTitle=graphGlobalFns.makeTextSVG({transform:"rotate(-90)",fill:"#33bad7"},this.yAxisTitle,10);
                gyAxisTitle.append(yAxisTitle);
                return gyAxisTitle;
        },
        getBars:function(dataObj){
            var xAxisDelta=this.xAxisWidth/(this.xAxisItemsNum+1);
            var barWidth=xAxisDelta/(2*this.ledgerItemsNum),count=0;
            var gBars=graphGlobalFns.makeSVG('g',{transform:"translate("+this.yAxisPad+","+this.yAxisHeight+")",
            'class':"CABars"});
            $.each(dataObj,function(user,stats){
                for(var k=0;k<stats.length;k++){
                    var val=stats[k].itemValue;
                    var y=(val/barLineGraphFn.yAxisMax)*barLineGraphFn.yAxisHeight;
                    var x=xAxisDelta*(k+1)-((-barLineGraphFn.ledgerItemsNum/2+(count+1))*barWidth);
                    gBars.append(graphGlobalFns.makeSVG('rect',{'stroke-width':0,stroke:'none',
                        fill:graphGlobalFns.ledgerColors[count],height:(y-(barLineGraphFn.xAxisStrokeWidth/2)),
                    width:barWidth,y:-y,x:x}));
                }
                count++;
            });
          return gBars;
        },
        getAreas:function(dataObj){
            var xAxisDelta=this.xAxisWidth/(this.xAxisItemsNum-1);
            if(this.fixedSpacingOfXAxisItems!=0){
                xAxisDelta=this.fixedSpacingOfXAxisItems;
            }

            var areasGx=this.yAxisPad+this.spaceFromYAxis;
            var gAreas=graphGlobalFns.makeSVG('g',{transform:"translate("+areasGx+","+(this.yAxisHeight-1)+")",
            'class':"CAAreas"});
            //the -1 is just to adjust the graph

            var count=0,gDummy=graphGlobalFns.makeSVG('g');
            $.each(dataObj,function(user,stats){
                var gCircles=graphGlobalFns.makeSVG('g');
                var polyline="",polygon="0,0 ";
                for(var k=0;k<stats.length;k++){
                    var val=stats[k].itemValue;
                    if(val==undefined)continue;
                    var y=-(val/barLineGraphFn.yAxisMax)*barLineGraphFn.yAxisHeight;
                    var x=xAxisDelta*k;
                    var circleColor=graphGlobalFns.areaLineColors[count];
                    if(y>0){
                        y=0;//fix for negative marks
                        circleColor="red";
                    }
                    polyline+=x+","+y+" ";
                    polygon+=x+","+(y-barLineGraphFn.lineAreaStrokeWidth)+" ";
                    var circle=graphGlobalFns.makeSVG('circle', {cx: x, cy:y,
                        r:4,fill:circleColor,'class':'areaCircle'});
                    circle.data("listData",stats[k].listData);
                    if(stats[k].listLink)circle.data("listLink",stats[k].listLink);
                    gCircles.append(circle);
                }
                var opacity=0.3;
                if(count==0)opacity=1;
                polygon+=((barLineGraphFn.xAxisItemsNum-1)*xAxisDelta)+",0";
                gAreas.append(graphGlobalFns.makeSVG('polyline',{stroke:graphGlobalFns.areaLineColors[count],
                'stroke-width':barLineGraphFn.lineAreaStrokeWidth,fill:"none",points:polyline}));
                if(count==0)gAreas.append(graphGlobalFns.makeSVG('polygon',
                {fill:graphGlobalFns.areaColors[count],points:polygon,'fill-opacity':opacity}));
                gDummy.append(gCircles);
                count++;
            });
            gAreas.append(graphGlobalFns.makeSVG('g',{'class':"gAreaInfo"}));
            gAreas.append(gDummy);
            //for showing info of circle
          return gAreas;
        },
        initializeEvents:function(svgEl){
            svgEl.on('mouseover',".areaCircle",this.areaCircleMouseover);
            svgEl.on('mouseout',".gAreaInfo",this.areaCircleMouseout);
        },
        areaCircleMouseover:function(){
            var CAAreas=$(this).closest(".CAAreas");
            var listData=$(this).data("listData");
            var listLink=$(this).data("listLink");
            var listLinkHeight=0;
            if(listLink)listLinkHeight=30;
            var x=$(this).attr("cx")-50;
            var y=parseInt($(this).attr("cy"))-(parseInt($(this).attr("r"))+10+(listData.length*15)+listLinkHeight);
            //10 for 5+5 padding of rect , 20*num for text
            //25 for width of rect
            var g=graphGlobalFns.makeSVG('g',{transform:"translate("+x+","+y+")","text-anchor":"middle"});
            var blackRectWidth=100;
            var blackRect=graphGlobalFns.makeSVG('rect',{width:blackRectWidth,height:listData.length*15+listLinkHeight+20,
                fill:"#373737",'fill-opacity':0.9});
            g.append(blackRect);
            var yValForRectTexts=0;
            for(var k=0;k<listData.length;k++){
                g.append(graphGlobalFns.makeTextSVG({x:blackRectWidth/2,y:k*15,'font-size':10,'fill':"#ffffff"},listData[k],15));
                yValForRectTexts=k*15;
            }
            if(listLink){
                var gLink=graphGlobalFns.makeSVG('g',{transform:"translate("+5+","+(yValForRectTexts+30)+")"});
                var link=graphGlobalFns.makeSVGLink({"xlink:href":listLink.href,"class":listLink["class"]}
                ,{"text-decoration":"underline","font-size":10,"fill":"#33bad7",x:blackRectWidth/2,y:(yValForRectTexts+30)},
                "view full Analytics");               
                   // g.append(link);
            }
            CAAreas.find(".gAreaInfo").html(g);
        },
        areaCircleMouseout:function(){
            var CAAreas=$(this).closest(".CAAreas");
            CAAreas.find(".gAreaInfo g").remove();
        },
        getLines:function(dataObj){

        },
        getLedger:function(dataObj){
          //height between each ledger item is fixed at 20px;
           var i=0;
            var gLedger=graphGlobalFns.makeSVG('g',{transform:"translate("+(graphGlobalFns.totalWidth-this.ledgerWidth)+","+graphGlobalFns.totalHeight/4+")",
            'class':"CALedger",'font-size':'10px',fill:"#373737"});
           $.each(dataObj,function(name,stats){
                var gLedgerItem=graphGlobalFns.makeSVG('g',{transform:"translate(0,"+i*20+")",'class':"CALedgerItem"});
                gLedgerItem.append(graphGlobalFns.makeSVG('rect',{'stroke-width':0,stroke:graphGlobalFns.ledgerColors[i],
                fill:graphGlobalFns.ledgerColors[i],height:10,width:22}));
                gLedgerItem.append(graphGlobalFns.makeTextSVG({x:25,y:0,stroke:"none"},name,9))
                   i++;
                gLedger.append(gLedgerItem);
            });
            this.ledgerItemsNum=i++;

            return gLedger;
        }
    }



var pieChartFn={
    sectorAngleArr:[],
    circleRadius:100,
    ledgerPadding:0,
    drawGraph:function(graphDetails,graphDimen,dataObj){
        if(graphDetails.title&&graphDetails.title!=""){
            graphGlobalFns.graphTitle=graphDetails.title;
        }
        if(graphDimen.width)graphGlobalFns.totalWidth=graphDimen.width;
        if(graphDimen.height)graphGlobalFns.totalHeight=graphDimen.height;
        graphGlobalFns.circleRadius= graphGlobalFns.totalHeight/2;

        //gets the pie chart angles and inserts %into the dataObj
        this.getPieChartAngles(dataObj);

        var gGraphArea=graphGlobalFns.makeSVG('g',{transform:"translate("+((graphGlobalFns.totalWidth/2)-this.ledgerPadding)+", \n\
        "+(graphGlobalFns.totalHeight/2)+")",'class':'pieChartGroup'});
        gGraphArea.append(this.getArcs()).append(graphGlobalFns.makeSVG('g',{'class':'pieDetails'}));

        var chartAreaDiv=graphGlobalFns.createSVGWrapper();
        var SVGEl=chartAreaDiv.find("svg");

        var ledgerWrapper=graphGlobalFns.makeSVG('g',{});
        if(graphDetails.putLedger==true){
            ledgerWrapper.append(this.getLedger(dataObj));
        }
        SVGEl.append(ledgerWrapper).append(gGraphArea).data("dataObj",dataObj);
        this.initializeEvents(SVGEl);
        return chartAreaDiv;
    },
    getPieChartAngles:function(pieData){
        var total=0,angleTotal=0,percentTotal=0;
        this.sectorAngleArr=[];
        for(var k=0;k<pieData.length;k++){
            total+=pieData[k].itemValue;
        }
        for(var i=0;i<pieData.length;i++){
            var angle=parseInt((pieData[i].itemValue/total)*360);
            var percent=parseInt((pieData[i].itemValue/total)*100);
            if(i==pieData.length-1){
                this.sectorAngleArr.push(360-angleTotal);
                pieData[i].percent= (100-percentTotal)+"%";
            }
            else {
             this.sectorAngleArr.push(angle);
             pieData[i].percent= percent+"%";
            }
            angleTotal+=angle;
            percentTotal+=percent;
        }
    },
    getArcs:function(){
            var startAngle=0,endAngle=0,detEndAngle=0,radius=graphGlobalFns.circleRadius;
            var startCoord=parseInt(0 + radius*Math.cos(Math.PI*startAngle/180));
            var endCoord = parseInt(0 + radius*Math.sin(Math.PI*startAngle/180));
            var x1,y1,x2,y2,x3,y3=0;
            var pieSVG=graphGlobalFns.makeSVG('g',{transform:"translate(0,0)"});
            for(var i=0; i <this.sectorAngleArr.length; i++){
                var angle=this.sectorAngleArr[i];
                var upDown,leftRight;
                startAngle = endAngle;
                endAngle = startAngle + angle;
                detEndAngle=startAngle+angle/2;
                x1 = parseInt(0 + radius*Math.cos(Math.PI*startAngle/180));
                y1 = parseInt(0 + radius*Math.sin(Math.PI*startAngle/180));

                x2 = parseInt(0 + radius*Math.cos(Math.PI*endAngle/180));
                y2 = parseInt(0 + radius*Math.sin(Math.PI*endAngle/180));

                x3=parseInt(0 + radius*Math.cos(Math.PI*detEndAngle/180));
                y3=parseInt(0 + radius*Math.sin(Math.PI*detEndAngle/180));

                if((endAngle>=0&&endAngle<=180)){
                    upDown="UP";
                }
                else {
                     upDown="DOWN";
                }
                if(endAngle<=270&&endAngle>=90){
                    leftRight="RIGHT";
                }
                else{
                    leftRight="LEFT";
                }

                if(i==this.sectorAngleArr.length-1){
                    x2=startCoord;
                    y2=endCoord;
                }
                var largeFlag=0;
                if(angle>180)largeFlag=1;

                var d = "M0,0  L" + x1 + "," + y1 + "  A"+radius+","+radius+" 0 "+largeFlag+",1 " + x2 + "," + y2 + " z"; //1 means clockwise
                var path=graphGlobalFns.makeSVG('path', {d:d, fill: graphGlobalFns.ledgerColors[i],'class':"piePart"});
                path.data("x3",x3).data("y3",y3).data("upDown",upDown).data("leftRight",leftRight);
                pieSVG.append(path);
            }
            return pieSVG;
    },
    getLedger:function(dataObj){
            var gLedger=graphGlobalFns.makeSVG('g',{transform:"translate("+(graphGlobalFns.totalWidth-125)+","+graphGlobalFns.totalHeight/4+")",
            'class':"CALedger",'font-size':'10px',fill:"#373737"});
            for(var k=0;k<dataObj.length;k++){
                var gLedgerItem=graphGlobalFns.makeSVG('g',{transform:"translate(0,"+k*20+")",'class':"CALedgerItem"});
                gLedgerItem.append(graphGlobalFns.makeSVG('rect',{'stroke-width':0,stroke:graphGlobalFns.ledgerColors[k],
                fill:graphGlobalFns.ledgerColors[k],height:10,width:22}));
                gLedgerItem.append(graphGlobalFns.makeTextSVG({x:25,y:0,stroke:"none"},dataObj[k].itemName,9))
                gLedger.append(gLedgerItem);
            }
            return gLedger;
    },
    initializeEvents:function(svgEl){
        svgEl.on('mouseover',".piePart",svgEl,this.piePartMouseover);
        svgEl.on('mouseout',".piePart",svgEl,this.piePartMouseout);
    },
    piePartMouseover:function(e){
            var $this=$(this),svgEl=e.data;
            var pieCakeData=svgEl.data("dataObj")[$this.index()];
            var $thisData=$this.data();
            var rectWidth=100,rectHeight=40,groupPosx=$thisData.x3,groupPosy=$thisData.y3;
            if($thisData.upDown=="DOWN"&&$thisData.leftRight=="LEFT"){
                groupPosy=groupPosy-rectHeight;
            }
            else if($thisData.upDown=="DOWN"&&$thisData.leftRight=="RIGHT"){
                groupPosy=groupPosy-rectHeight;
                groupPosx=groupPosx-rectWidth;
            }
            else if($thisData.upDown=="UP"&&$thisData.leftRight=="RIGHT"){
                groupPosx=groupPosx-rectWidth;
            }
            var group=graphGlobalFns.makeSVG('g',{transform:"translate("+groupPosx+","+groupPosy+")",
                'text-anchor':"middle",'font-size':10,fill:"#373737",'font-weight':"bold"});

            var whiteRect=graphGlobalFns.makeSVG('rect',{width:rectWidth,height:rectHeight,fill:"#efefef"});
            var textName=graphGlobalFns.makeTextSVG({x:50,y:0},pieCakeData.itemName,15);
            var textPercent=graphGlobalFns.makeTextSVG({x:50,y:15},pieCakeData.percent,15);
            group.append(whiteRect).append(textName).append(textPercent);
            svgEl.find(".pieDetails").html(group);
    },
    piePartMouseout:function(e){
            e.data.find(".pieDetails g").remove();
    }
}
