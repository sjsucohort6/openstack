<#import "masterTemplate.ftl" as layout />

<@layout.masterTemplate title="OpenStack Cloud Service Console" moduleName="openstackApp">

<div class="row">
    <div class="col-md-12">
        <div class="row">
            <div class="col-md-12">
                <div class="row">
                    <div class="col-md-12">
                        <table id="serviceTable" class="table table-striped table-bordered" cellspacing="0"
                               width="100%">
                            <thead>
                            <tr>
                            <#--<th>
                                #
                            </th>-->
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
                            </tr>
                            </thead>
                            <tfoot>
                            <tr>
                            <#--<th>
                                #
                            </th>-->
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
                            </tr>
                            </tfoot>

                            <tbody>
                                <#list services as service>
                                <tr>
                                <#--<td>
                                    <div class="radio">
                                        <input type="radio" name="serviceName">
                                    </div>
                                </td>-->
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
                                </tr>
                                </#list>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <input type="submit" class="btn btn-primary btn-sm active" data-toggle="modal"
                               data-target="#addServModal" value="Add Service"
                               name="addService">
                        <input type="submit" class="btn btn-primary btn-sm active" value="Delete Service"
                               name="deleteService">
                    </div>
                </div>
            </div>
        </div>
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
                        <button type="button" class="btn btn-primary">Save changes</button>
                    </div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div><!-- /.modal -->
        <div class="row">
            <div class="col-md-12">
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