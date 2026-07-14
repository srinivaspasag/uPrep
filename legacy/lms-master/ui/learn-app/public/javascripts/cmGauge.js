/**
 * Created by hale on 2016/12/29.
 */

;(function ($, window, document, undefined) {
  
  var Gauge = function (el) {
    this.$element = el,
      this.defaults = {},
      this.options = $.extend({}, this.defaults, {})
  };
  
  Gauge.prototype = {
    colors: ['gauge-green', 'gauge-orange', 'gauge-yellow', 'gauge-blue', 'gauge-red'],
    partSize: 0,
    initParams: function () {
      var colorLen = Gauge.prototype.colors.length;
      Gauge.prototype.partSize = 100.0 / colorLen;
    },
    createGauge: function (elArray) {
      elArray.each(function () {
        Gauge.prototype.updateGauge($(this));
      });
      
      elArray.bind('updateGauge', function (e, num) {
        $(this).data('percentage', num);
        Gauge.prototype.updateGauge($(this));
      });
    },
    updateGauge: function (el) {
      Gauge.prototype.initParams();
      var percentage = el.data('percentage');
      percentage = (percentage > 100) ? 100 : (percentage < 0) ? 0 : percentage;
      console.log("guage called");
      var color;
      if(percentage < 40){
        color = Gauge.prototype.colors[4];
      }
      if(percentage >= 40 && percentage < 70){
        color = Gauge.prototype.colors[1]; 
      }
      if(percentage >= 70){
        color = Gauge.prototype.colors[0]; 
      }
      color = color;
      el.css('transform', 'rotate(' + ((1.8 * percentage) - 90) + 'deg)');
      el.parent()
        .removeClass(Gauge.prototype.colors.join(' '))
        .addClass(color);
    }
  };
  
  $.fn.cmGauge = function () {
    var gauge = new Gauge(this);
    return gauge.createGauge(this);
  }
  
})(jQuery, window, document);