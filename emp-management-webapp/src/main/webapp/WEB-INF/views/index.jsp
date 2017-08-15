<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page isELIgnored="false" %> 
<html>
    <head>
        <title>Employee Management</title>
        <link href="<c:url value='/resources/font-awesome-4.7.0/css/font-awesome.css'/>" rel="stylesheet"/>
    </head>
    
    <style>
        table.solid, tr.solid > td {
            border: 1px solid black;
            border-collapse: collapse;
        }
        
        th {
             border: 3px solid black;
        }

        a.icon-block {
            display:inline-block;
            /*width:10em;*/
            text-align:center;
            color: red;
        }
    </style>
    
    <body>
        <span style="font-size: 12px;">Language : <a href="?language=en">English</a>|<a href="?language=ph">Filipino</a></span><br/>
        <span style="font-size: 12px;">Current Locale : ${pageContext.response.locale}</span>
        <h1 style="text-align: center;">Employee Management</h1>
        
        
            <div style="width: 80%;margin: auto">
                <form action="home" method="Post">
                    <table width="100%;">
                        <tr>
                            <td colspan="2" style="text-align: right">
                                <i style="color: red;">${searchResult}</i>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <select name="sortBy" id="sortBy">
                                    <option value="3" ${sortBy == 3 ? 'selected="selected"' : ''}>
                                        <spring:message code='field.lastName'/>
                                    </option>
                                    <option value="1" ${sortBy == 1 ? 'selected="selected"' : ''}>
                                        GWA
                                    </option>
                                    <option value="2" ${sortBy == 2 ? 'selected="selected"' : ''}>
                                        <spring:message code='field.dateHired'/>
                                    </option>
                                </select>
                                <button type="button" onClick="sortList();"><spring:message code='message.sort'/></button>
                            </td>
                            <td style="text-align: right;">
                                <input name="searchId" type="number" min="1"/>
                                <input type="submit" name="search" value="<spring:message code='message.search'/>"/>
                            </td>
                        </tr>
                    </table>
                    <br/>
                    <div style="height: 70%;">
                        <table width="100%" style="text-align: center;" class="solid" id="personList"></table>
                    </div>
                    <div style="text-align: center;color: green;" id="deleteResult">${deleteResult}</div>
                </form>
                <div style="text-align: right;">
                    <button onclick="location.href='add-person'" type="button"><spring:message code='message.newEmployee'/></button>
                    <button onclick="location.href='roles'" type="button"><spring:message code='message.manageRoles'/></button>
                </div>
            </div>
    </body>

    <script src="<c:url value='/resources/js/jquery-2.1.1.js'/>"></script>
    <script>
        function clicked(e) { if(!confirm('Are you sure?')){e.preventDefault(); return}}

        $().ready(function(){
            sortList();
        });

        function sortList() {
            var response = "<tr class='solid'><th>ID</th><th><spring:message code='field.lastName' text='Last Name' /></th>" +
                        "<th><spring:message code='field.firstName' text='First Name' /></th><th>GWA</th><th><spring:message code='field.dateHired' text='Date Hired' /></th><th><spring:message code='message.delete' text='Delete' /></th></tr>";
            $.ajax({
                url : 'populate_list',
                method : 'GET',
                contentType: 'application/json; charset=utf-8',
                data :{"sortBy" : $("#sortBy").val()},
                dataType:'json',
                success: function (data) {
                    //populate list
                    if(data.length > 0) {
                        $.each(data,function(key, obj){
                            response += "<tr id='person-" + obj["id"] + "' class='solid'><td><a href='home/" + obj["id"] + "' style='text-decoration: none; color:inherit;'>" + obj["id"] + "</a></td>";
                            
                            response += "<td>" + obj["name"]["lastName"] + "</td>";
                            response += "<td>" + obj["name"]["firstName"] + "</td>";
                            response += "<td>" + obj["gwa"] + "</td>";
                            response += "<td>" + obj["dateHiredToString"] + "</td>";

                            response += "<td><a onclick='deletePerson(" + obj['id'] + ")'; class='icon-block' style='cursor: pointer;color:red;' class='icon-block'><i class='fa fa-trash-o' aria-hidden='true'></i></a></td></tr>";
                        });
                    } else {
                        response += "<tr class='solid'><td colspan='6' style='color: red;'>No persons found</td></tr>";
                    }

                    $("#personList").html(response);      
                }
            });
        }

        function deletePerson(id) {
            if(confirm('Are you sure?')) {
                $.ajax({
                    url : 'delete_person',
                    method : 'GET',
                    contentType: 'application/json; charset=utf-8',
                    data :{"delete" : id},
                    dataType:'json',
                    success: function (data) {
                        $("#person-"+id).remove();
                        $("#deleteResult").html("<i style='color: green;'>" + data["deleteResult"] + "</i>");      
                    }
                });
            }
        }
    </script>
</html> 