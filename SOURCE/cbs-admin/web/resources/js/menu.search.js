function initTopMenu() {
    $('#navbar-search-input').keypress(function (event) {
        if (event.which == 38 || event.which == 40 || event.which == 13) {
            event.preventDefault();
        }

        searchTopMenuNew($('#navbar-search-input').val(), this);
    });
}

function initLeftMenu() {
    $('#sidebar .sidebar-form .input-group input').keypress(function (event) {
        if (event.which == 38 || event.which == 40 || event.which == 13) {
            event.preventDefault();
        }

        searchLeftMenuNew($('#sidebar .sidebar-form .input-group input').val(), this);
    });
}

function searchLeftMenu(criteria) {
    //Nothing
}

function searchLeftMenuNew(criteria, inputElm) {
    $('#menu-search').show();
    $('#menu-search li.dropdown').addClass('open');
    $('#menu-search li.dropdown li').remove();
    var menuResults = $('#menu-search ul.dropdown-menu[role="menu"]');

    if (criteria != null && criteria.length >= 1) {
        criteria = criteria.toLowerCase();
        var match = false;
        $('ul.sidebar-menu.tree li:not(.treeview, .header)').each(function () {
            var linkText = $(this).find('a span');
            if (linkText && typeof linkText.html() !== 'undefined' && removeSign(linkText.html()).indexOf(removeSign(criteria)) !== -1) {
                var menuItem = $(this).clone();
                menuItem.removeClass('active');
                menuResults.append(menuItem);
                match = true;
            }
        });

        if (!match) {
            $('#menu-search li.dropdown').removeClass('open');
            $('#menu-search').hide();

        } else {
            initSearchResultKey(inputElm);
        }

    } else {
        $('#menu-search li.dropdown').removeClass('open');
        $('#menu-search').hide();
    }
}

////////////////////////////////////////////////////////////////////////////

function searchTopMenu(criteria) {
    //Nothing
}

function searchTopMenuNew(criteria, inputElm) {
    $('#menu-search').show();
    $('#menu-search li.dropdown').addClass('open');
    $('#menu-search li.dropdown li').remove();
    var menuResults = $('#menu-search ul.dropdown-menu[role="menu"]');

    if (criteria != null && criteria.length >= 1) {
        criteria = criteria.toLowerCase();
        var match = false;

        $('ul.nav.navbar-nav ul.dropdown-menu[role="menu"] > li').each(function () {
            var linkText = $(this).find('a span:not(.label)');
            if (linkText && typeof linkText.html() !== 'undefined' && removeSign(linkText.html()).indexOf(removeSign(criteria)) !== -1) {
                menuResults.append($(this).clone());
                match = true;
            }
        });
        if (!match) {
            $('#menu-search li.dropdown').removeClass('open');
            $('#menu-search').hide();

        } else {
            initSearchResultKey(inputElm);
        }

    } else {
        $('#menu-search li.dropdown').removeClass('open');
        $('#menu-search').hide();
    }
}

////////////////////////////////////////////////////////////////////////////

function initSearchResultKey(inputElm) {
    var rs = $("#menu-search .dropdown-menu");
    var li = $('#menu-search .dropdown-menu .tls_module');
    var liSelected;
    var next;

    $(inputElm).unbind("keydown");
    $(inputElm).keydown(function (e) {
        if (e.which === 40) {
            if (liSelected) {
                liSelected.children().first().removeClass('menu-hover');
                next = liSelected.next();

                if (next.length > 0) {
                    liSelected = next;
                    liSelected.children().first().addClass('menu-hover');

                } else {
                    liSelected = li.first();
                    liSelected.children().first().addClass('menu-hover');
                }

            } else {
                liSelected = li.first();
                liSelected.children().first().addClass('menu-hover');
            }

        } else if (e.which === 38) {
            if (liSelected) {
                liSelected.children().first().removeClass('menu-hover');
                next = liSelected.prev()

                if (next.length > 0) {
                    liSelected = next;
                    liSelected.children().first().addClass('menu-hover');

                } else {
                    liSelected = li.last();
                    liSelected.children().first().addClass('menu-hover');
                }
            } else {
                liSelected = li.last();
                liSelected.children().first().addClass('menu-hover');
            }

        } else if (e.which === 13) {
            window.location.href = liSelected.children().first().attr("href");
        }
    });
}

////////////////////////////////////////////////////////////////////////////