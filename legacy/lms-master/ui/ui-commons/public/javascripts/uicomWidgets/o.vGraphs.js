var vGraphs = new (function($) {
    var xAxisWidth, yAxisHeight;
    var yAxisWidth;
    var m = makeHTMLTag;
    var vgMarker;


    this.init = function(holder, dataObj) {
        //xAxis
        initXAxis(dataObj);

        //yAxis               
        initYAxis(dataObj);

        vgMarker = $("#vgMarker");

        if (dataObj.plotData)
            plotData = dataObj.plotData;
        //eventHandlers
        holder.on("mouseenter", ".vgAreaCircle", vgItemHover)
        holder.on("mouseleave", ".vgAreaCircle", vgItemLeave)
        holder.on("mouseenter", ".vgBar", vgItemHover)
        holder.on("mouseleave", ".vgBar", vgItemLeave)
    };
    var initXAxis = function(dataObj) {
        var xAxisData = dataObj.xAxis;
        if (xAxisData.title)
            xAxisTitle = xAxisData.title
        else
            xAxisTitle = "";
        xAxisTitlePos = xAxisData.titlePosition || "DOWN";

        xAxisWidth = xAxisData.width || 280;
        xAxisItemsList = xAxisData.items || [];
        xAxisRotateAngle = xAxisData.rotateTextBy || 0;
        xAxisTextWidth = xAxisData.textWidth || null;
    }
    var initYAxis = function(dataObj) {
        var yAxisData = dataObj.yAxis;
        if (yAxisData.title)
            yAxisTitle = yAxisData.title
        else
            yAxisTitle = "";
        plotReverse = yAxisData.plotReverse || false;

        yAxisItemsList = yAxisData.items || [];
        yAxisMax = yAxisData.yAxisMax || 100;
        yAxisMin = yAxisData.yAxisMin || 0;
        yItemsCount = yAxisData.yItemsCount || 6;
        if (yAxisItemsList.length == 0) {
            var delta = (yAxisMax - yAxisMin) / (yItemsCount - 1);
            for (var k = 0; k < yItemsCount; k++) {
                var pushNum = (plotReverse) ? (yItemsCount - k - 1) : k;
                yAxisItemsList.push(yAxisMin + (delta * pushNum));
            }
        } else {
            yItemsCount = yAxisItemsList.length;
            yAxisMax = (plotReverse) ? yAxisItemsList[0] : yAxisItemsList[yItemsCount - 1];
            yAxisMin = (plotReverse) ? yAxisItemsList[yItemsCount - 1] : yAxisItemsList[0];
        }
        yAxisHeight = yAxisData.height || 210;
        yAxisWidth = yAxisData.width || 25;
    }




    var vgItemHover = function() {
        var $this = $(this), $data = $this.data();
        var index = $this.attr("circle-index");
        var vgxaItem = $this.closest(".vGraph").find(".vgxaItem").eq(index);
        vgxaItem.addClass("vgxaItemHovered").siblings().removeClass("vgxaItemHovered");
        $this.css("opacity", "1").siblings().css("opacity", "0.6");
        var offy = $this.offset();
        var offsetForRadius = parseInt($this.attr("r")) + parseInt($this.attr("stroke-width")) - 2;
        vgMarker.css({left: (offy.left + offsetForRadius),
            top: (offy.top + offsetForRadius), height: Math.abs($this.attr("cy"))}).removeClass("nonner");

        if ($data.callback) {
            if ($data.callback == "default" && $data.infoHTML) {
                var html = $this.data("infoHTML");
                var offset = $this.offset();
                var infoDiv = $("#vgInfoDiv");
                floaterDiv = infoDiv;
                infoDiv.find(".vgInfo").html(html);
                var top = offset.top - infoDiv.height();
                var left = offset.left - (infoDiv.width() / 2) + ($this.get(0).getBoundingClientRect().width / 2);
                infoDiv.css("left", left).css("top", top);
            } else
                window[$data.callback]($this);
        }
    };
    var vgItemLeave = function() {
        var $this = $(this);
        var vgxaItem = $this.closest(".vGraph").find(".vgxaItem");
        vgxaItem.removeClass("vgxaItemHovered");
        $this.css("opacity", "0.6");
        vgMarker.addClass("nonner");
    };

    //xaxis
    var barLineGap = 20, xItemsCount, barLinePaWidth, xAxisTitle = "", maxBarLinePaWidth,
            xAxisItemsList = [], xAxisTextWidth, xAxisRotateAngle = 0, xAxisTitlePos;
    var drawXAxis = function() {
        xItemsCount = xAxisItemsList.length;
        barLinePaWidth = (xAxisWidth - ((xItemsCount + 1) * barLineGap)) / xItemsCount;
        maxBarLinePaWidth = (xAxisWidth - ((xItemsCount + 1) * barLineGap)) / 5;
        if (barLinePaWidth > maxBarLinePaWidth)
            barLinePaWidth = maxBarLinePaWidth;
        var top = 3, width = "auto", textLeft = 0;
        if (xAxisTextWidth) {
            width = xAxisTextWidth;
            //textLeft will align in it in center or in axis with the point
            if (xAxisTextWidth > barLinePaWidth)
                textLeft = (xAxisTextWidth - barLinePaWidth) / 2;
            if (xAxisRotateAngle != 0) {
                top = ((xAxisTextWidth - 16) / (180 / Math.abs(xAxisRotateAngle))) - 3;
            }
        } else {
            if (xAxisRotateAngle != 0) {
                top = ((barLinePaWidth - 16) / (180 / Math.abs(xAxisRotateAngle))) - 3;
            }
        }
        //3px is for adjustment purpose;


        var holder = m("div");
        for (var k = 0; k < xItemsCount; k++) {
            var left = (((k * barLinePaWidth) + ((k + 1) * barLineGap))) + "px";
            var itemHTML = xAxisItemsList[k], textDiv = m("div").html(itemHTML);
            holder.append("<div class='vgxaItem' style='left:" + left + ";\n\
            width:" + barLinePaWidth + "px;'>\n\
            <span class='vgxaItemText' style='top:" + top + "px;width:" + width + "px;left:-" + textLeft + "px;\n\
            -moz-transform:rotate(" + xAxisRotateAngle + "deg);\n\
            -webkit-transform:rotate(" + xAxisRotateAngle + "deg)'\n\
            -ms-transform:rotate(" + xAxisRotateAngle + "deg)'\n\
            transform:rotate(" + xAxisRotateAngle + "deg)'\n\
            title='" + textDiv.text() + "'>" + itemHTML + "</span></div>");
        }
        return m("div", {"class": "vGraphXAxis"}).html(holder.children());
    }
    var getXAxisTitle = function() {
        var className = "vgxaTitleDown";
        var xTitle = m("div");
        if (xAxisTitlePos == "SIDE") {
            className = "vgxaTitleSide";
            xTitle.css("top", (yAxisHeight - 8) + "px").css("left", (xAxisWidth + 5) + "px");
        }
        if (xAxisTitle != "") {
            xTitle.text(xAxisTitle).addClass(className);
        }
        return xTitle;
    }


    //y axis
    var yAxisMax, yAxisMin, yItemsCount, yAxisItemsList = [], plotReverse;
    var yAxisTitle = "";
    var drawYAxis = function() {
        var holder = m("div");
        var delta = (yAxisHeight / (yItemsCount - 1));
        for (var k = 0; k < yItemsCount; k++) {
            holder.append("<div class='vgyaItem' style='bottom:" + ((delta * k) - 8) + "px'>\n\
            " + yAxisItemsList[k] + "</div>");//to counter line height -8 is usesd
        }
        if (yAxisTitle != "") {
            var top = (yAxisHeight / 2) - 8; //to counter line height -8 is usesd
            var left = (yAxisHeight / 2) + 15;//for padding
            var yTitle = m("div", {"class": "vgyaTitle", "style": "top:" + top + "px;left:-" + left + "px;width:" + yAxisHeight + "px"})
                    .text(yAxisTitle);
            holder.append(yTitle);
        }
        return m("div", {"class": "vGraphYAxis", "style": "width: " + yAxisWidth + "px; \n\
        height: " + yAxisHeight + "px;"}).html(holder.children());
    }


    //bars    
    var drawBars = function() {
        var barPas = m("div");
        for (var x = 0; x < xItemsCount; x++) {
            var barPa = m("div", {"class": "vgBarDiv"});
            var left = ((x * barLinePaWidth) + ((x + 1) * barLineGap)) + "px";
            barPa.css("left", left).width(barLinePaWidth);
            var userCount = plotData.length, vgBarWidth = barLinePaWidth / userCount;
            for (var i = 0; i < userCount; i++) {
                var eachItem = plotData[i].items[x], val = eachItem.val;
                if(!val){
                    val=0;
                }
                var percent = parseInt(val * 100 / yAxisMax);
                var tileHeight = (percent * yAxisHeight) / 100;
                var bar = m("div", {"class": "vgBar"}).html("<div class='vgBarText'></div>\n\
                <div class='vgBarTile' style='height:" + tileHeight + "px;background-color:" + ledgerColors[i] + ";\n\
                 width:" + vgBarWidth + "px'></div>");
                if (userCount == 1) {
                    bar.find(".vgBarText").text(percent + "%");
                } else {
                    var $data = bar.data();
                    $data["infoHTML"] = eachItem.infoHTML;
                    $data["info"] = eachItem.info;
                    $data["callback"] = eachItem.callback;
                    bar.addClass("floaterSrc");
                }
                barPa.append(bar);
            }
            barPas.append(barPa);
        }
        return m("div", {"class": "vGraphArea", "style": "height:" + yAxisHeight + "px"})
                .html(barPas.children());
    }
    this.createColumnGraph = function(holder, dataObj) {
        prepareAxes(holder, dataObj);
        $(drawBars()).insertBefore(holder.find(".vGraphXAxis"));
    }


    //areas
    var plotData;
    var drawAreas = function() {
        var gAreas = makeSVG('g', {'class': "vgAreas", transform: "translate(0," + (yAxisHeight + 10) + ")"});
        var gCirclesHolder = makeSVG('g', {'class': "vgCirclesHolder", transform: "translate(0," + (yAxisHeight + 10) + ")"});
        //10 to account for the padding at the top,for eg to show circles for 100%.

        //-20 to account for huge gap between y axis and area graph
        for (var p = 0; p < plotData.length; p++) {
            var areaNeed = false;
            if (p == 0)
                areaNeed = true;
            var isStrokeNeeded = false;
            if (plotData[p].isStrokeNeeded) {
                isStrokeNeeded = plotData[p].isStrokeNeeded;
            }
            var areaAndCircles = getGAreaAndCircles(plotData[p], areaNeed, p, isStrokeNeeded);
            gAreas.append(areaAndCircles.gArea);
            gCirclesHolder.append(areaAndCircles.gCircles);
        }
        var rect = makeSVG("rect", {x: 0, y: 0, height: yAxisHeight, width: xAxisWidth, fill: "transparent"});
        var svg = makeSVG('svg', {style: "overflow: hidden' xmlns='http://www.w3.org/2000/svg'",
            version: '1.1', height: (yAxisHeight + 20), width: xAxisWidth}).html(rect).append(gAreas).append(gCirclesHolder);
        return m("div", {"class": "vGraphArea", "style": "height:" + yAxisHeight + "px"}).html(svg);
        //7px extra height to make for the overflow of circles when the marks are zero.
    }
    var getGAreaAndCircles = function(dataset, areaNeed, index, isStrokeNeeded) {
        var gArea = makeSVG('g', {'class': "vgArea"});
        var gCircles = makeSVG('g', {'class': "vgCircles"});
        //the -1 is just to adjust the graph

        var polyline = "", polygon = (barLineGap + (barLinePaWidth / 2)) + ",0 ";
        var items = dataset.items;
        var lineColor = dataset.lineColor || areaLineColors[index];
        var areaColor = dataset.areaColor || areaColors[index];
        var circleColor = dataset.circleColor || "#CC9900";
        for (var k = 0; k < xItemsCount; k++) {
            var eachItem = items[k], val = eachItem.val;
            var x = ((k * barLinePaWidth) + ((k + 1) * barLineGap) + barLinePaWidth / 2);            
            //it is agreed that for data points which have to be skipped, 
            //the 'undefined' value will be sent and in any other case
            //val will be assumed to be zero;
            if (val === undefined) {
                continue;
            } else if (!val) {
                //handles for zero,null,NaN,'' cases
                val = 0;
            }
            var y = (plotReverse) ? -(yAxisHeight - (val / yAxisMax) * yAxisHeight) : -(val / yAxisMax) * yAxisHeight;

            var circleStrokeColor = circleColor;
            if (y >= 0) {
                y = 0;//fix for negative marks
                circleStrokeColor = "#CC3333";
            }
            polyline += x + "," + y + " ";
            polygon += x + "," + (y + 1) + " ";
            var circle = makeSVG('circle', {cx: x, cy: y, "circle-index": k,
                r: 5, fill: "transparent", "stroke-width": 4, stroke: circleStrokeColor,
                'class': 'vgAreaCircle floaterSrc'});

            var $data = circle.data();
            $data["infoHTML"] = eachItem.infoHTML;
            $data["info"] = eachItem.info;
            $data["callback"] = eachItem.callback;
            gCircles.append(circle);
        }
        var strokeOpacity = 0;
        if (isStrokeNeeded) {
            strokeOpacity = 1;
        }
        polygon += (((xItemsCount - 1) * barLinePaWidth) + (xItemsCount * barLineGap) + barLinePaWidth / 2) + ",0";
        gArea.append(makeSVG('polyline', {stroke: lineColor, 'stroke-width': 1,
            fill: "none", points: polyline, opacity: strokeOpacity}));
        var opacity = 0;
        if (areaNeed)
            opacity = 1;

        gArea.append(makeSVG('polygon', {fill: areaColor, points: polygon, opacity: opacity}));
        return {gArea: gArea, gCircles: gCircles};
    }
    this.createAreaGraph = function(holder, dataObj) {
        prepareAxes(holder, dataObj);
        var xAxisDiv = holder.find(".vGraphXAxis");
        $(drawAreas()).insertBefore(xAxisDiv);
        //adjustments for area graph 
        xAxisDiv.css({position: "relative", left: "-20px"});
        holder.find("svg").css({position: "relative", left: "-20px", top: "-10px"});
    }
    this.appendToAreaGraph = function(holder, dataSet) {
        var polylines = holder.find("polyline"), index = polylines.length;
        for (var k = 0; k < areaLineColors.length; k++) {
            var foundColor = false;
            for (var p = 0; p < polylines.length; p++) {
                if (polylines.eq(p).attr("stroke") == areaLineColors[k]) {
                    foundColor = true;
                    break;
                }
            }
            if (!foundColor) {
                index = k;
                break;
            }
        }
        var areaAndCircles = getGAreaAndCircles(dataSet, false, index, true);
        holder.find(".vgAreas").append(areaAndCircles.gArea);
        holder.find(".vgCirclesHolder").append(areaAndCircles.gCircles);
        return([areaAndCircles.gArea, areaAndCircles.gCircles]);
    };


    //utilities
    var ledgerColors = ["#8DCDEC", "#00cc00", "#0055cc", "#8800cc", "#d9007e", "#ff0000", "#ff9900", "#ffcc00", "#ffff00", "#ace600"],
//    areaColors=["#fafab5","#bee9af","#afe9e4","#bfcdf1"],
//    areaLineColors=["#b9b889","#90b683","#74aaa5","#808aa3"];   
            areaColors = ["#FFFF8D", "#90b683", "#bee9af", "#afe9e4", "#bfcdf1"],
            areaLineColors = ["#CC3333", "#339933", "#66cc66", "#33cccc", "#3366cc"],
            circeColors = ["#CC9900", "#339933", "#CC3333"];
    var makeSVG = function(tag, attrs) {
        var el = document.createElementNS("http://www.w3.org/2000/svg", tag);
        for (var k in attrs)
            el.setAttribute(k, attrs[k]);
        return $(el);
    }
    var prepareAxes = function(holder, dataObj) {
        vGraphs.init(holder, dataObj);
        var vGraph = m("div", {"class": "vGraph"});
        vGraph.append(drawYAxis());

        var xAxisAndAreaHolder = m("div", {"style": "width:" + xAxisWidth + "px;\n\
        margin-left:" + yAxisWidth + "px;position:relative"}).html(drawXAxis()).append(getXAxisTitle());
        vGraph.append(xAxisAndAreaHolder);
        holder.html(vGraph);
        return holder;
    }
})(jQuery)
$.fn.createAreaGraph = function(dataObj) {
    vGraphs.createAreaGraph($(this), dataObj);
};
$.fn.appendToAreaGraph = function(dataObj) {
    return vGraphs.appendToAreaGraph($(this), dataObj);
};
$.fn.createColumnGraph = function(dataObj) {
    vGraphs.createColumnGraph($(this), dataObj);
};







