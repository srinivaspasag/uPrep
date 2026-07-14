$(".dropdown").hover(
  function() {
      $('.dropdown-menu', this).stop( true, true ).delay(200).fadeIn(500);
      $(this).toggleClass('open');
  },
  function() {
      $('.dropdown-menu', this).stop( true, true ).delay(200).fadeOut(500);
      $(this).toggleClass('open');
});