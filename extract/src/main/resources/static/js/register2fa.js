function cancel2faRegistration() {
    var form = $('#registrationForm');
    form.attr('action', cancelUrl);
    form.submit();
}

$(function() {
    $('#cancelRegistrationButton').on('click', function() {
        cancel2faRegistration();
    });
});
