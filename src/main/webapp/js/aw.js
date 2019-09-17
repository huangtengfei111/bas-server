/*
 This file is a collection of unobtrusive JS that binds to link_to generated anchors typical for Ajax calls.

 author: 
 */

$(document).ready(function() {
    $('a[data-link]').bind('click', function() {
        var anchor = $(this);
        var destination = anchor.attr("data-destination");
        var formId = anchor.attr("data-form");
        var href = anchor.attr("href");
        var _method = anchor.attr("data-method");
        var before = anchor.attr("data-before");
        var after = anchor.attr("data-after");
        var beforeArg = anchor.attr("data-before-arg");
        var afterArg = anchor.attr("data-after-arg");
        var error = anchor.attr("data-error");

        var confirmMessage = anchor.attr("data-confirm");

        if(confirmMessage != null ){
            if(!confirm(confirmMessage)){
                return false;
            }
        }

        //not Ajax
        if(destination == null && before == null && after == null && (_method == null || _method.toLowerCase() == "get")){
            return true;
        }

        if (_method == null) {
            _method = "get";
        }
        var type;
        if (_method.toLowerCase() == "get") {
            type = "get";
        } else if (_method.toLowerCase() == "post"
                || _method.toLowerCase() == "put"
                || _method.toLowerCase() == "delete") {
            type = "post";
        }

        var data = "_method=" + _method;
        if (formId != null) {
            data += "&" + $("#" + formId).serialize();
        }

        if(before != null){
            eval(before)(beforeArg);
        }


        $.ajax({ url: href, data: data, type:type,
            success: function(data) {
                if (after != null)
                    eval(after)(afterArg, data);

                if (destination != null)
                    $("#" + destination).html(data);
            },
            error: function(xhr, status, errorThrown) {
                if(error != null){
                    eval(error)(xhr.status, xhr.responseText );
                }
            }
        });

        return false;
    });
});
