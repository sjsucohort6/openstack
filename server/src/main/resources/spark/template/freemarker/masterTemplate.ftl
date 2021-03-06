<#macro masterTemplate title="OpenStack" moduleName="openstackApp">
<!DOCTYPE html>
<!--
  ~ Copyright (c) 2015 San Jose State University.
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a copy
  ~ of this software and associated documentation files (the "Software"), to deal
  ~ in the Software without restriction, including without limitation the rights
  ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  ~ copies of the Software, and to permit persons to whom the Software is
  ~ furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  -->

<html lang="en" ng-app=${moduleName}>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>${title}</title>

    <meta name="description" content="Source code generated using layoutit.com">
    <meta name="author" content="LayoutIt!">

    <link href="../css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="../DataTables/datatables.min.css"/>
    <link href="../css/style.css" rel="stylesheet">

</head>
<body>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <div class="page-header">
                <h1>
                    OpenStack Cloud Service Console <small>"Platforms as Services" - on OpenStack Cloud.</small>
                </h1>
            </div>
        </div>
    </div>
    <div class="body">
        <#nested />
    </div>
    <div class="footer">
        <i>
            Copyright &copy; 2015 <a href="http://www.sjsu.edu">San Jose State University</a>,
            <br>
            All Rights Reserved.
        </i>
    </div>
</div>
<script src="../js/jquery.min.js"></script>
<script src="../js/bootstrap.min.js"></script>
<script type="text/javascript" src="../DataTables/datatables.min.js"></script>
<script src="../js/jquery.rest.min.js"></script>
<script src="../js/scripts.js"></script>
</body>
</html>
</#macro>