// Initialize JQuery REST client
var restClient = new $.RestClient('/virtapp/api/v1.0/');
restClient.add("services", {stripTrailingSlash: true, stringifyData: true});
restClient.add("quota", {stripTrailingSlash: true});
var serviceClient = restClient.services; // maps to /services
var quotaClient = restClient.quota; // maps to /quota

// Initialize the 2 data tables
$(document).ready(function() {
    $('#serviceTable').DataTable();
    $('#taskTable').DataTable();
    var request = quotaClient.read();
    request.done(function (data, textStatus, xhrObject){
        $('#quota').val(data);
    });
} );

//show service info upon click on Get Service button
$('#getService').on('click',function(){
    //var serviceName=$(this).closest('tr').children()[0].textContent.trim();
    // This line is required to fix the issue of modal() not a function error
    jQuery.noConflict();
    var serviceName= $('table#serviceTable input[type=radio]:checked').val();
    var request = serviceClient.read(serviceName);
    request.done(function (data, textStatus, xhrObject){
      //alert('I have data: ' + JSON.stringify(data));
      $("textarea#serviceJson").val( JSON.stringify(data, null, 4) );
      $("#serviceInfoModal").modal("show");
    });
});

// Prompt for service deletion confirmation on click of Delete Service button
$("#deleteService").on("click", function(){
    jQuery.noConflict();
    // Show confirmation modal
    $('#confirmDeleteModal').modal("show");ss
});

// Handle deletion of service
$("#confirmDelete").on('click', function (){
    var serviceName= $('table#serviceTable input[type=radio]:checked').val();
    alert(serviceName);
    var request = serviceClient.del(serviceName);
    request.done(function (data, textStatus, xhrObject){
           alert("service " + serviceName + "is being deleted");
           $("#deletionStatus").text("Service " + serviceName + " is being terminated!");
        });
});

// handle service add on click of Add Service button
$("#addService").on("click", function(){
    jQuery.noConflict();
    $("#serviceAddModal").modal("show");
});

$("#addServicePost").on("click", function(event){
     event.preventDefault();
     var serviceName = $('form input#serviceName').val();
     var serviceType = $('form select#serviceType :selected').text();
     var network = $('form select#network :selected').text();
     var numNodes = $('form select#numNodes :selected').text();

     var servicePayload = '{'
                        + '"name" : "' + serviceName + '",'
                        + '"serviceType" : "' + serviceType + '",'
                        + '"networkName" : "' + network + '",'
                        + '"nodes" : [';
     for (i = 1; i <= numNodes; i++) {
        if ((numNodes == 1) || (i < numNodes)) {
            // create web VMs
            servicePayload += '{'
                            + '"flavorName" : "m1.small",'
                            + '"imageName" : "UBUNTU-WEB-IMG",'
                            + '"type" : "WEB"'
                            + '}';
        } else {
            // create a db VM
            servicePayload += '{'
                            + '"flavorName" : "m1.small",'
                            + '"imageName" : "UBUNTU-DB-IMG",'
                            + '"type" : "DB"'
                            + '}';
        }
        if (i < numNodes) {
            servicePayload += ',';
        }
     }
     servicePayload += ']}';
     alert(servicePayload);
     var request = serviceClient.create(JSON.parse(servicePayload));
     request.done(function (data, textStatus, xhrObject){
            alert("service " + serviceName + "is being created");
            $("#addServiceStatus").text("Service " + serviceName + " is being created!");
            // Disable Add button
            $("#addService").attr('disabled', 'disabled');
         });
     request.fail(function() {
         alert( "Service create request failed" );
         $("#addServiceStatus").text("Service " + serviceName + " create request failed!");
       });
 });