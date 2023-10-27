/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

$().ready(function () {
    initTopMenu();
    initLeftMenu();
    reOrderPnlControl();
});

////////////////////////////////////////////////////////////////////////////

PrimeFaces.locales["vi"] = {
    closeText: "Thoát",
    prevText: "Tháng Trước",
    nextText: "Tháng Sau",
    monthNames: ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"],
    monthNamesShort: ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"],
    dayNames: [ "Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"],
    dayNamesShort: ["CN","T2", "T3", "T4", "T5", "T6", "T7"],
    dayNamesMin: [ "CN", "T2", "T3", "T4", "T5", "T6", "T7"],
    weekHeader: "Tuần",
    firstDay: 0,
    isRTL: false,
    showMonthAfterYear: false,
    yearSuffix: "",
    timeOnlyTitle: "Chỉ giờ",
    timeText: "Thời gian",
    hourText: "Giờ",
    minuteText: "Phút",
    secondText: "Giây",
    currentText: "Ngày hiện tại",
    ampm: false,
    month: "Tháng",
    week: "Tuần",
    day: "Ngày",
    allDayText: "Cả ngày"
};

////////////////////////////////////////////////////////////////////////////

function handleSubmitRequest(id, xhr, status, args) {
    if (args.errorProcess || args.validationFailed) {
        jQuery(id).effect("shake", {
            times: 3
        }, 100);
    } else {
        PF(id).hide();
    }
}

////////////////////////////////////////////////////////////////////////////

function clearFilters(id) {
    PF(id).clearFilters();
}

////////////////////////////////////////////////////////////////////////////

function downloadStart() {
    PF('statusDialog').show();
}

////////////////////////////////////////////////////////////////////////////

function downloadStop() {
    PF('statusDialog').hide();
}

////////////////////////////////////////////////////////////////////////////

function adminMaterial() {
    //Nothing
}

////////////////////////////////////////////////////////////////////////////

function removeSign(str) {
    str = str.toLowerCase();
    str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a");
    str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e");
    str = str.replace(/ì|í|ị|ỉ|ĩ/g, "i");
    str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o");
    str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u");
    str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y");
    str = str.replace(/đ/g, "d");
    str = str.replace(/^\-+|\-+$/g, "");
    return str;
}

////////////////////////////////////////////////////////////////////////////

function resizeIframe(obj) {
    obj.style.height = (obj.contentWindow.document.body.scrollHeight + 25) + 'px';
}

////////////////////////////////////////////////////////////////////////////

function scrollTreeTo(id) {
    scrollDivTo(".ui-tree-container", id);
}

////////////////////////////////////////////////////////////////////////////

function scrollDivTo(wrapper, id) {
    var wrapperOffset = $(wrapper).offset().top;
    //console.log(wrapperOffset);

    var scrollerOffset = $(wrapper).scrollTop();
    //console.log(scrollerOffset);

    var etop = $("[id='" + id + "']").offset().top + scrollerOffset - wrapperOffset;
    //console.log(etop);

    $(wrapper).animate({
        scrollTop: etop
    }, 1000);
}

////////////////////////////////////////////////////////////////////////////

function scrollBodyTo(id) {
    var etop = $("[id='" + id + "']").offset().top;
    $('html, body').animate({
        scrollTop: etop
    }, 1000);
}

////////////////////////////////////////////////////////////////////////////

function scrollToTop() {
    $('html, body').animate({
        scrollTop: 0
    }, 1000);
}

////////////////////////////////////////////////////////////////////////////

function scrollToBottom(id) {
    var selector = $("[id='" + id + "']");
    $(selector).animate({
        scrollTop: $(selector).scrollHeight
    }, 1000);
}

function reOrderPnlControl() {
    try {
        let firsBtn
        $(".pnl-control div").each(function () {
            if($(this)[0].outerHTML.includes("ui-button")) {
                firsBtn = $(this);
                return false;
            }
        });

        $(".pnl-control div").each(function () {
            let html;
            if ($(this).html().trim() == '') {
                html = $(this)[0].outerHTML;
                $(this).remove();
                $(firsBtn).before(html);
            }
        });

        $(".pnl-control").css("opacity", "1");

    } catch (ex) {
        console.log(ex);
    }
}