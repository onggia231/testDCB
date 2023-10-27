function checkAllCollumns(id, value) {
    //Get right check ids
    var checkIds = $("[id='form_main:table_module_right']").find(".right-chkbox").map(function () {
        return this.id;
    }).get();

    //Get class name
    var className = $("[id='" + id.replace('_input', '') + "'] span").attr('class');

    //Set right check
    for (i = 0; i < checkIds.length; i++) {
        if (id.substring(0, id.indexOf("j_idt")) === checkIds[i].substring(0, checkIds[i].indexOf("j_idt"))) {
            $("[id='" + checkIds[i] + "_input']").val(value);
            $("[id='" + checkIds[i] + "'] span").attr('class', className);

            if (value != 0) {
                $("[id='" + checkIds[i] + "'] .ui-chkbox-box").addClass('ui-state-active');
            } else {
                $("[id='" + checkIds[i] + "'] .ui-chkbox-box").removeClass('ui-state-active');
            }
        }
    }
}

function initCheckBoxRight() {
    //Init var
    var state = 1;
    var chkState = [];

    //Set style class
    chkState[0] = "ui-chkbox-icon ui-c";
    chkState[1] = "ui-chkbox-icon ui-c ui-icon ui-icon-check";
    chkState[2] = "ui-chkbox-icon ui-c ui-icon ui-icon-closethick";

    //Reset check collumn
    $(".check-col span").attr('class', chkState[0]);

    //Header click event
    $("[id='form_main:table_module_right'] .ui-chkbox").click(function () {
        //Get id header
        var idCheckAll = this.id;

        //Get style class check box header
        var styleClass = $("[id='" + idCheckAll + "']").attr("class");

        //Get all access right check box
        var ids = $("[id='form_main:table_module_right']").find(".right-chkbox").map(function () {
            return this.id;
        }).get();

        //If check all
        if (styleClass.indexOf("check-all") > 0) {
            for (i = 0; i < ids.length; i++) {
                $("[id='" + ids[i] + "_input']").val(state);
                $("[id='" + ids[i] + "'] span").attr('class', chkState[state]);
                $(".check-all span").attr('class', chkState[state]);
                $(".check-col span").attr('class', chkState[state]);
                $(".check-row span").attr('class', chkState[state]);

                if (state != 0) {
                    $("[id='" + ids[i] + "'] .ui-chkbox-box").addClass('ui-state-active');
                    $(".check-all .ui-chkbox-box").addClass('ui-state-active');
                    $(".check-col .ui-chkbox-box").addClass('ui-state-active');
                    $(".check-row .ui-chkbox-box").addClass('ui-state-active');
                } else {
                    $("[id='" + ids[i] + "'] .ui-chkbox-box").removeClass('ui-state-active');
                    $(".check-all .ui-chkbox-box").removeClass('ui-state-active');
                    $(".check-col .ui-chkbox-box").removeClass('ui-state-active');
                    $(".check-row .ui-chkbox-box").removeClass('ui-state-active');
                }
            }
        }

        //If check all row
        else if (styleClass.indexOf("check-row") > 0) {
            var rowIdNumber = idCheckAll.match("module_right:(.+)");
            rowIdNumber = rowIdNumber[1].substr(0, rowIdNumber[1].lastIndexOf(":"));

            for (i = 0; i < ids.length; i++) {
                if (ids[i].indexOf(rowIdNumber) > 0) {
                    $("[id='" + ids[i] + "_input']").val(state);
                    $("[id='" + ids[i] + "'] span").attr('class', chkState[state]);

                    if (state != 0) {
                        $("[id='" + ids[i] + "'] .ui-chkbox-box").addClass('ui-state-active');
                    } else {
                        $("[id='" + ids[i] + "'] .ui-chkbox-box").removeClass('ui-state-active');
                    }
                }
            }
        }

        //Set state
        state = state + 1;
        if (state === 3) {
            state = 0;
        }
    });
}