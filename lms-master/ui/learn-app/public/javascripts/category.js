var categorySections = new function(){
    var parDiv;
    var orgId;
    var CLICK = "click.categorySections";
    var CHANGE = "change.categorySections";
    var scope = "OPEN";
    var name = "";
    var categoryName = "";
    var SIZE = 50;
    var start = 0;
    var popupOpacityLevel = 0.7;
    this.get = function(par){
        parDiv = par.find("#categoryProgramsSection");
        parDiv.off(CLICK);
        parDiv.on(CLICK,".loadMoreSections",getSections);
        parDiv.on(CLICK,".siteProgramHolder",openProgPopup);
        parDiv.on(CLICK,".readMore",openProgPopup);
        parDiv.on(CHANGE,".chooseCategory",function(){
            start = 0;
            name = $(this).val();
            getSections();
        });
        orgId = parDiv.data("orgId");
        categoryName = parDiv.data("categoryName");
        getCategories(function(){
            start = 0;
            getSections();
        });
    };
    this.extGet = function(url,holder,params,cbFn){
        getSectionsByHolder(url,holder,params,cbFn);
    }
    var getCategories = function(cbFn){
        var params = {
            orgId : orgId,
            userId : "PUBLIC",
            callingUserId : "PUBLIC"
        };
        $.get("/Register/getCategories",params,function(data){
            var catFound = false;
            if(data && data.result && !data.errorCode){
                list = data.result.list;
                if(list.length>0){
                    var select = parDiv.find(".chooseCategory");
                    for(catIndex in list){
                        var cat = list[catIndex];
                        var option = document.createElement("option");
                        option = $(option);
                        option.prop("value",cat.name);
                        if (categoryName && !catFound) {
                            var cnameParam = categoryName.toLowerCase();
                            var cnameOption = cat.name.toLowerCase();
                            if (cnameOption.indexOf(cnameParam) > -1) {
                                catFound = true;
                                name = cat.name;
                                option.prop("selected", true);
                            }
                        }
                        option.html(cat.name);
                        select.append(option);
                    }
                    if(cbFn) cbFn(list);
                }
            }
        });
    };
    var getSections = function(){
        var holder = parDiv.find(".categoryProgramsContainer");
        var params = {
            name : name,
            orgId : orgId,
            scope : scope,
            start : start,
            size : SIZE,
            orderBy : "timeCreated"
        };
        var url = "/Institute/getCategorySections";
        getSectionsByHolder(url,holder,params,function(){
            checkForLoadMore(parDiv);
        });
    };
    var getSectionsByHolder = function(url,holder,params,cbFn){
        // showLoading(holder);
        $.get(url,params,function(data){
            if(params.start == 0){
                holder.html(data);
            }else{
                holder.append(data);
            }
            // hideLoading(holder);
            // doWookMark(holder);
            if(cbFn){
                try{ cbFn();}catch(err){}
            }
        });
    };
    var showLoading = function(holder){
        var div = "<div class='centerText loadingIcon'><img src='/public/images/microsite/loading.gif' alt='Loading'/></div>"
        if(start == 0){
            holder.html(div);
        }else{
            holder.append(div);
        }
    };
    var hideLoading = function(holder){
        holder.find(".loadingIcon").remove();
    }
    var checkForLoadMore = function(parentDiv){
        var totalHitsElem = parentDiv.find("#totalPrograms");
        var totalHits = parseInt(totalHitsElem.val());
        var loadMoreDiv = parentDiv.find(".loadMoreSections");
        if(totalHits > (start + SIZE) ){
            loadMoreDiv.removeClass("nonner");
            start += SIZE;
        }else{
            loadMoreDiv.addClass("nonner");
        }
        totalHitsElem.remove();
    }
    var doWookMark = function(holder){
        var items = holder.find(".siteProgramHolder");
        if(items.length>0){
        items.wookmark({
                // Prepare layout options.
            align: 'left',
                autoResize: true, // This will auto-update the layout when the browser window is resized.
                container: holder, // Optional, used for some extra CSS styling
                offset: 13, // Optional, the distance between grid items
                outerOffset: 0, // Optional, the distance to the containers border
                //itemWidth: 210 // Optional, the width of a grid item
            });
        }
    };
    var openProgPopup = function(){
        var sectionId = $(this).data("sectionId");
        var params = {
            orgId : orgId,
            sectionId : sectionId
        };
        $.get("/Institute/getSingleProgramPopup",params,function(data){
            swal({
                html:data,
                showConfirmButton:false
            });
        });
    };
};
