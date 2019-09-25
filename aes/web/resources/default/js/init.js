(function($) {
  $(function() {
    $(document).ready(function() {
      $(".sidenav").sidenav();
    });
  }); // end of document ready
  
  $("#menu-toggle").click(function(e) {
      e.preventDefault();
      $("#wrapper").toggleClass("toggled");
    });
})(jQuery); // end of jQuery name space
