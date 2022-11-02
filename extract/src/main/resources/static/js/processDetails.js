/* 
 * Copyright (C) 2017 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



/**
 * Sends the data about the current process to the server for adding or updating.
 */
function submitProcessData() {
    //update usersIds in hidden input before saving process 
    var usersListIdsArray = $('#users').select2('val');
    $('#usersIds').val(usersListIdsArray
                            .filter((value) => value.startsWith('user-'))
                            .map((value) => value.substring('user-'.length)).join(','));
    $('#userGroupsIds').val(usersListIdsArray
                                .filter((value) => value.startsWith('group-'))
                                .map((value) => value.substring('group-'.length)).join(','));

    $('.parameter-select').each(function (index, item) {
        var idsArray = $(item).select2('val');
        var selectId = $(item).attr('id');
        var valuesFieldId = selectId.substring(0, selectId.length - '-select'.length);
        var valuesField = document.getElementById(valuesFieldId);
        $(valuesField).val(idsArray.join(','));
    });

    //update tasks position
    $(".extract-proc-tasks .col-xl-8 .card-body .row .taskPosition").each(function(i) {
        $(this).val(i + 1);
    });
    //submit form
    $('#processForm').submit();
}

function deleteTask(taskcard, id) {
    
    if (id === null || id < 0) {
        return;
    }

    var deleteConfirmTexts = LANG_MESSAGES.processTask.deleteConfirm;
    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = deleteConfirmTexts.message;
    var confirmedCallback = function() {
        taskcard.css("background-color","white");
        taskcard.fadeOut(400, function(){
            taskcard.hide();
        });
        var href = $('#processForm').attr('action', $('#deleteTaskButton-' + id).attr('href'));
        $("#htmlScrollY").val( $(document).scrollTop());
        $('#processForm').attr('action', $('#deleteTaskButton-' + id).attr('href'));
        submitProcessData();
    };
    showConfirm(deleteConfirmTexts.title, message, confirmedCallback, null, alertButtonsTexts.yes, alertButtonsTexts.no);
}

$(function() {
    
    var readOnly = $("#readonly").val();

    var scrollY = $("#htmlScrollY").val();
    if(scrollY !== null && !isNaN(scrollY)) {
        $(document).scrollTop(scrollY);
        $("#htmlScrollY").val(0);
    }

    function formatUserItem(item) {

        if(!item.id) {
            return item.text;
        }

        const icon = (item.id.startsWith('group-')) ? 'fa-users' : 'fa-user';
        return $(`<span><i class="fa ${icon}"></i>&nbsp;${item.text}</span>`);
    }

    $(".parameter-select.select2").select2({
        multiple:true
    });

    $(".user-select.select2").select2({
        templateSelection: formatUserItem,
        templateResult: formatUserItem,
        multiple:true
    });

    //set users in the multiple select
    var usersIdsArray = $("#usersIds").val().split(',').map((value) => `user-${value}`);
    var userGroupsIdsArray = $("#userGroupsIds").val().split(',').map((value) => `group-${value}`);
    $('#users').val([...usersIdsArray, ...userGroupsIdsArray]);
    $('#users').trigger('change');

    $(".parameter-select-values").each(function (index, item) {
        var idsArray = $(item).val().split(',');
        console.log("Values:", idsArray);
        var selectId = $(item).attr("id") + "-select";
        var select2Item = $(document.getElementById(selectId));
        $(select2Item).val(idsArray);
        console.log("Select ID:", selectId, "Value:", $(select2Item).val());
        $(select2Item).trigger('change');
    });

    //initialisation de la tooltip "help"
    /*$('[data-toggle="popover"]').popover({
        container: 'body',
        placement: 'top',
        html: 'true',
        content : $(this).attr("content")
    });*/
     
    $('.helplink').popover({
            html : true,
            container : 'body',
            trigger : 'manual',
            placement: 'auto',
            title: function() {
                var content = $(this).attr("href");
                var popupHeader = $(content).children(".popover-heading").clone();
                return $(popupHeader).wrapAll("<div/>").parent().html();
            },
            content: function() {
                var content = $(this).attr("href");
                var popupBody = $(content).children(".popover-body").clone();
                
                return popupBody.wrapAll("<div/>").parent().html();
            }
    }).click(function(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        $('.popover').hide();
        $(this).popover('show');
    });
    
    $(document).on("click",".popup-header .img-close", function() {
       $('.helplink').popover('hide');
   });
   
    if(readOnly == "false") {
        //Gestion du drag and drop pour reordonner les tâches d'un process
        $('.extract-proc-tasks .col-xl-8 .card-body .row:first').sortable({
            items: '.taskcard',
            helper: 'clone',
            handle: '.card-header',
            placeholder: 'placeholder',
            refreshPositions: true,
            opacity: 0.9,
            scroll: true,
            over: function(event, ui) {
                const cl = ui.item.attr('class');
                $('.placeholder').addClass(cl);
            },
            create : function(event, ui) {
                //sortRules();
            },
            start : function(e, ui) {
                $(e.target.id + ' .task-arrow-down').css('display', 'none');
                 $(".extract-proc-tasks .col-xl-8 .card-body .row .taskcard").css('margin-top', '20px');
                //ui.item.find('.task-arrow-down').css('display','none');
                //ui.placeholder.height(ui.item.children().height());
            },
            stop : function(e, ui) {
                var $currentItemActive = ui.item.find('.btn-toggle.active input');
                $(".extract-proc-tasks .col-xl-8 .card-body .row .taskcard").css('margin-top', '');
                
                $(".extract-proc-tasks .col-xl-8 .card-body .row .task-arrow-down").each(function(i) {
                    //display arrow down
                    $(this).css('display','none');
                    if(i > 0)
                        $(this).css('display','block');
                });
                $currentItemActive.prop("checked", true);
            }
        });

        //Gestion du drag and drop pour l'ajout d'une tâche de la droite vers la gauche
        $(".extract-proc-tasks  .col-xl-4 .available-task").draggable({
            helper: 'clone',
            revert : 'invalid'
        });
        $(".extract-proc-tasks .col-xl-8 .card-body:first").droppable({
            accept: ".extract-proc-tasks  .col-xl-4 .available-task",
            hoverClass: "panel-droppable-hilight",
            tolerance: "touch",
            drop : function(e, ui){
               //submit to server
                var href = ui.helper.attr('href');
                $('#processForm').attr('action', href);
                submitProcessData();
            }
        });

        $('.deletetask-button').on('click', function() {
            var $button = $(this);
            var idTask = parseInt($button.attr('id').replace('deleteTaskButton-', ''));

            if (isNaN(idTask) || $button.hasClass('disabled')) {
                return;
            }

            var taskcard = $button.closest('.chosed-task');

            deleteTask(taskcard, idTask);
        });
    }
});