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
    var usersIdsArray = $("#users").select2('val');
    $("#usersIds").val(usersIdsArray.join(','));
    //update tasks position
    $(".extract-proc-tasks .col-lg-8 .panel-body .row .taskPosition").each(function(i) {
        $(this).val(i + 1);
    });
    //submit form
    $('#processForm').submit();
}

function deleteTask(taskPanel, id) {
    
    if (id === null || id < 0) {
        return;
    }

    var deleteConfirmTexts = LANG_MESSAGES.processTask.deleteConfirm;
    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = deleteConfirmTexts.message;
    var confirmedCallback = function() {
        taskPanel.css("background-color","white");
        taskPanel.fadeOut(400, function(){
            taskPanel.hide();
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
    
    $("#users").select2({
        multiple:true
    });
    
    //set users in the multiple select
    var usersIdsArray = $("#usersIds").val().split(',');
    $('#users').val(usersIdsArray);
    $('#users').trigger('change');

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
        $(".extract-proc-tasks .col-lg-8 .panel-body .row:first").sortable({
            items: '.taskpanel',
            helper: 'clone',
            handle: '.panel-heading',
            placeholder: "placeholder",
            refreshPositions: true,
            opacity: 0.9,
            scroll: true,
            over: function(event, ui) {
                var cl = ui.item.attr('class');
                $('.placeholder').addClass(cl);
            },
            create : function(event, ui) {
                //sortRules();
            },
            start : function(e, ui) {
                $(e.target.id + ' .task-arrow-down').css('display', 'none');
                 $(".extract-proc-tasks .col-lg-8 .panel-body .row .taskpanel").css('margin-top', '20px');
                //ui.item.find('.task-arrow-down').css('display','none');
                //ui.placeholder.height(ui.item.children().height());
            },
            stop : function(e, ui) {
                var $currentItemActive = ui.item.find('.btn-toggle.active input');
                $(".extract-proc-tasks .col-lg-8 .panel-body .row .taskpanel").css('margin-top', '');
                
                $(".extract-proc-tasks .col-lg-8 .panel-body .row .task-arrow-down").each(function(i) {
                    //display arrow down
                    $(this).css('display','none');
                    if(i > 0)
                        $(this).css('display','block');
                });
                $currentItemActive.prop("checked", true);
            }
        });

        //Gestion du drag and drop pour l'ajout d'une tâche de la droite vers la gauche
        $(".extract-proc-tasks  .col-lg-4 .available-task").draggable({
            helper: 'clone',
            revert : 'invalid'
        });
        $(".extract-proc-tasks .col-lg-8 .panel-body:first").droppable({
            accept: ".extract-proc-tasks  .col-lg-4 .available-task",
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

            var taskPanel = $button.closest('.chosed-task');

            deleteTask(taskPanel, idTask);
        });
    }
});