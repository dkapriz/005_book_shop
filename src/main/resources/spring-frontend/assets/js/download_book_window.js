$(document).ready(function () {
    $('a.btn').click(function (event) {
        event.preventDefault();
        $('#overlay').fadeIn(297, function () {
            $('#book-download-popup').css('display', 'block').animate({opacity: 1}, 198);
        });
    });

    $('#book-download-popup__close, #overlay').click(function () {
        $('#book-download-popup').animate({opacity: 1}, 198, function () {
           $(this).css('display', 'none');
           $('#overlay').fadeOut(297);
        });
    });
});
