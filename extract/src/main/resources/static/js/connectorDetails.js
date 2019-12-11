/**
 * @file Contains the methods to process the form used to edit or add a connector.
 * @author Yves Grasset
 */

/**
 * Add row to the rules table .
 *
 */
function addRule() {
    var $button = $(this);
    var link = $button.attr('href');
    $('#connectorForm').attr("action", link);
    $('#connectorForm').submit();
    
}



/**
 * Deletes a connector.
 * 
 * @param {object}  tr      The row table identifying the rule to delete
 * @param {int}     id      The identifier of the rule to delete
 * @param {string}  rule    The rule of the connector to delete (only for display purposes)
 */
function deleteRule(tr, id, rule) {
    
    if (id === null || id < 0) {
        return;
    }

    var IdConnector = $(tr).find("#connector_id").val();
    var deleteConfirmTexts = LANG_MESSAGES.rulesList.deleteConfirm;
    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    if(rule)
        rule = "\""+rule+"\"";
    var message = deleteConfirmTexts.message.replace('\{0\}', rule);
    var confirmedCallback = function() {
        tr.css("background-color","#FF3700");
        tr.fadeOut(400, function(){
            tr.hide();
            tr.find("input.ruleTag").val("DELETED");
            tr.find("input.rulePosition").val("-9999"); 
        });
        $('#connectorForm').attr('action', $('#deleteButton-' + id).attr('href'));
        $('#connectorForm').submit();
    };
    showConfirm(deleteConfirmTexts.title, message, confirmedCallback, null, alertButtonsTexts.yes, alertButtonsTexts.no);
}



function sortRules() {
    var sortableList = $('#rulesTable tbody');
    var listitems = $('tr', sortableList);

    listitems.sort(function(a, b) {

        return (parseInt($(a).find('.rulePosition').val()) > parseInt($(b).find('.rulePosition').val())) ? 1 : -1;
    });

    sortableList.append(listitems);
}



$(function() {
    $('.delete-button').on('click', function() {
        var $button = $(this);
        var id = parseInt($button.attr('id').replace('deleteButton-', ''));
        
        if (isNaN(id)) {
            return;
        }

        var tr = $button.closest('tr');
        var rule = tr.find('td.ruleCondition > textarea').val();

        deleteRule(tr, id, rule);
    });
    
    $(".ruleProcess select").on('change', function() {
        var idProcess = $(this).find(":selected").val();
        var hidden = $(this).closest('td.ruleProcess').find("input[type='hidden']");
        hidden.val(idProcess);
    });

    $("#rulesTable tbody").sortable({
        items: 'tr',
        create : function(event, ui) {
            sortRules();
        },
        helper: function(e, tr) {
            var $originals = tr.children();
            var $helper = tr.clone();
            $helper.children().each(function(index) {
                $(this).width($originals.eq(index).width())
            });
            return $helper;
        },
        stop : function(e, ui) {
            var $currentItemActive = ui.item.find('.btn-toggle.active input');
            $('td.ruleReorder .rulePosition').each(function(i) {
                $(this).val(i + 1);
            })
            $currentItemActive.prop("checked", true);
        }
    });
    
    var ruleHelpHref = $("#popup-over-rulehelp .popover-body").attr('href');
    $("#popup-over-rulehelp .popover-body").load(ruleHelpHref);
    
    //Initialize help window for rule
    $('#rulesTable .helplink').popover({
            html : true,
            container : 'body',
            trigger : 'manual',
            placement: 'auto',
            title: function() {
                var content = $(this).attr("data-popover-content");
                var popupHeader = $(content).children(".popover-heading").clone();
                return $(popupHeader).wrapAll("<div/>").parent().html();
            },
            content: function() {
                var content = $(this).attr("data-popover-content");
                var popupBody = $(content).children(".popover-body").clone();
                
                return popupBody.wrapAll("<div/>").parent().html();
            }
    }).click(function(evt) {
        evt.stopPropagation();
        $(this).popover('show');
    });
    /*$('html').click(function() {
        if ($('.popover').has(e.target).length === 0) {
            $('#rulesTable .fa-info-circle').popover('hide');
        }
    });*/
    /*$('#rulesTable .fa-info-circle').bind('shown.bs.popover', function (e) {
        $('#popup-over-rulehelp').show();
    }) ;*/
    // Fermeture de la popup au clic sur la croix
   $(document).on("click",".popup-header .img-close", function() {
       $('#rulesTable .helplink').popover('hide');
   });
   /*$('#rulesTable .fa-info-circle').click( function (e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).popover('show');	
   });*/
    
});



/**
 * Sends the data about the current connector to the server for adding or updating.
 */
function submitConnectorData() {
    $('#connectorForm').submit();
}
