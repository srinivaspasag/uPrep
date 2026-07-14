function digitPercisionString(digit,point){
    digit = digit.toString();
    if(digit.length < point){
        var pre = "";
        for(i = digit.length;i<point;i++){
            pre += "0";
        }
        digit = pre + digit;
    }else if(digit.length > point){
        digit = digit.toPrecision(point);
    }
    return digit;
}

new function($){
    Date.locale = {
           en: {
            month_names: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September',
             'October', 'November', 'December'],
            month_names_short: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
           }
    };
    $.fn.formatDate = function(format,lang){
        lang = lang?lang:"en";
        $(this).each(function(){
        var dateTime = parseInt(this,10);
        if(!dateTime || dateTime<0){
            format = "";
            return;
        }
        var date = new Date(dateTime);
        format = format.replace("dd",date.getDate());
        format = format.replace("mmm",Date.locale[lang].month_names[date.getMonth()]);
        format = format.replace("mm",digitPercisionString(date.getMonth()+1,2));
        format = format.replace("yyyy",date.getFullYear());
        format = format.replace("yy",date.getYear());
        format = format.replace("hrs",date.getHours());
        format = format.replace("min",digitPercisionString(date.getMinutes(),2));
        format = format.replace("sec",digitPercisionString(date.getSeconds(),2));
        });
        return format;
    };
    $.fn.updateClientTime = function(className,format,dt){
        className = className ? className : ".updateClientTime";
        $(this).each(function(){
            $(this).find(className).each(function(){
                var $this = $(this);
                var frm = $this.data("format");
                frm = frm ? frm : (format ? format : "dd/mm/yyyy hrs:min:sec");
                var dtHtml = $([$this.data("time")]).formatDate(frm);
                $this.replaceWith(dtHtml);
            });
        });
    };
    $.fn.dateWiseTime = function(){
        var dt = this;
        try{
            if(this.get(0)){
                dt = this.get(0);
            }
        }catch(er){}
        dt.setHours(0);
            dt.setMinutes(0);
            dt.setSeconds(0);
            dt.setMilliseconds(0);
        return dt;
    };
}(jQuery);