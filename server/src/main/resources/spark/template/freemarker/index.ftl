<#import "masterTemplate.ftl" as layout />

<@layout.masterTemplate title="OpenStack Cloud Service Console" moduleName="openstackApp">

<div class="row">
    <div class="col-md-12">
        <div class="row">
            <div class="col-md-12">
                <div class="row">
                    <div class="col-md-12">
                        <h2>Resource usage for user "${user}" of tenant "${tenant}"</h2>
                        <div class="well" id="quota">
                            <#if quota??>
                                <pre>
                                    ${quota}
                                </pre>
                            </#if>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <h2>Services</h2>
                        <table id="serviceTable" class="table table-striped table-bordered" cellspacing="0"
                               width="100%">
                            <thead>
                            <tr>
                                <th>
                                    Service Name
                                </th>
                                <th>
                                    Tenant
                                </th>
                                <th>
                                    Service Type
                                </th>
                                <th>
                                    Status
                                </th>
                                <th>
                                    Select
                                </th>
                            </tr>
                            </thead>
                            <tfoot>
                            <tr>
                                <th>
                                    Service Name
                                </th>
                                <th>
                                    Tenant
                                </th>
                                <th>
                                    Service Type
                                </th>
                                <th>
                                    Status
                                </th>
                                <th>
                                    Select
                                </th>
                            </tr>
                            </tfoot>
                            <tbody>
                                <#list services as service>
                                <tr>
                                    <td>
                                    ${service.name}
                                    </td>
                                    <td>
                                    ${service.tenant}
                                    </td>
                                    <td>
                                    ${service.serviceType}
                                    </td>
                                    <td>
                                        <#if service.status == 'READY'>
                                            <p class="bg-success">${service.status}</p>
                                        <#elseif service.status == 'FAILED'>
                                            <p class="bg-danger">${service.status}</p>
                                        <#else>
                                        ${service.status}
                                        </#if>

                                    </td>
                                    <td>
                                        <input type="radio" name="services" value="${service.name}"
                                               style="vertical-align: middle; margin: 0px;">
                                    </td>
                                </tr>
                                </#list>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <input type="button" class="btn btn-primary btn-sm active" value="Add Service"
                               id="addService">
                        <input type="button" class="btn btn-primary btn-sm active" value="Get Service"
                               id="getService">
                        <input type="button" class="btn btn-primary btn-sm active" value="Delete Service"
                               id="deleteService">

                        <div id="deletionStatus" class="text-danger">

                        </div>
                    </div>
                </div>
            </div>
        </div>
        <hr>
        <!-- Service info modal. -->
        <div class="modal fade" id="serviceInfoModal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title">Service Details</h4>
                    </div>
                    <div class="modal-body">
                        <p>
                            <textarea id="serviceJson" class="form-control" rows="5" disabled>
                            </textarea>
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                        <#--<button type="button" class="btn btn-primary">Save changes</button>-->
                    </div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div><!-- /.modal -->

        <!-- Service Add modal. -->
        <div class="modal fade" id="serviceAddModal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <form role="form">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                    aria-hidden="true">&times;</span></button>
                            <h4 class="modal-title">Add Service</h4>
                        </div>
                        <div class="modal-body">
                            <div class="form-group">
                                <label for="serviceName">Service Name</label>
                                <input type="text" class="form-control" id="serviceName">
                            </div>
                            <div class="form-group">
                                <label for="serviceType">Service Name</label>
                                <select class="form-control" id="serviceType">
                                    <option>BASIC</option>
                                    <option>BIG</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="network">Network</label>
                                <select class="form-control" id="network">
                                    <#list networks as network>
                                        <option>${network}</option>
                                    </#list>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="numNodes">Number of Nodes (1 DB Node is created if number of nodes is >
                                    1)</label>
                                <select class="form-control" id="numNodes">
                                    <option>1</option>
                                    <option>2</option>
                                    <option>3</option>
                                </select>
                            </div>

                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                            <button type="submit" class="btn btn-primary" id="addServicePost">Add</button>
                            <div id="addServiceStatus"></div>
                        </div>
                    </form>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div><!-- /.modal -->

        <!-- Confirm delete service -->
        <div id="confirmDeleteModal" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title">Confirm Service Deletion</h4>
                    </div>
                    <div class="modal-body">
                        Are you sure?
                    </div>
                    <div class="modal-footer">
                        <button type="button" data-dismiss="modal" class="btn btn-primary" id="confirmDelete">Delete
                        </button>
                        <button type="button" data-dismiss="modal" class="btn">Cancel</button>
                    </div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div>

        <div class="row">
            <div class="col-md-12">
                <h2>Tasks</h2>
                <table id="taskTable" class="table table-striped table-bordered" cellspacing="0" width="100%">
                    <thead>
                    <tr>

                        <th>
                            Task Name
                        </th>
                        <th>
                            Tenant
                        </th>
                        <th>
                            Time
                        </th>
                        <th>
                            Message
                        </th>
                    </tr>
                    </thead>
                    <tfoot>
                    <tr>

                        <th>
                            Task Name
                        </th>
                        <th>
                            Tenant
                        </th>
                        <th>
                            Time
                        </th>
                        <th>
                            Message
                        </th>
                    </tr>
                    </tfoot>
                    <tbody>
                        <#list tasks as task>
                        <tr>
                            <td>
                                <#if task.jobName??>${task.jobName}<#else>NULL</#if>
                            </td>
                        <#-- <td>
                         ${task.jobDataMap}
                         </td>-->
                            <td>
                            ${task.tenantName}
                            </td>
                            <td>
                            ${task.startTime?datetime}
                            </td>
                            <td>
                            ${task.message}
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

</@layout.masterTemplate>