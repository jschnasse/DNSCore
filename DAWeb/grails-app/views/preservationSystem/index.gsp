
<%@ page import="daweb3.PreservationSystem" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'preservationSystem.label', default: 'PreservationSystem')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class=page-body>
			<div class="nav" role="navigation">
				<ul>
					<li><a class="listl" href="${createLink(uri: '/cbtalk/index')}"><g:message message="zurück zur Administrationsseite"/></a></li>
				</ul>
			</div>
			<a href="#list-preservationSystem" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
			<div id="list-preservationSystem" class="content scaffold-list" role="main">
				<h1><g:message code="default.list.label" args="[entityName]" /></h1>
				<g:if test="${flash.message}">
					<div class="message" role="status">${flash.message}</div>
				</g:if>
			  	<div style="overflow:auto">
					<table>
						<thead>
							<tr>
							
								<g:sortableColumn property="minRepls" title="${message(code: 'preservationSystem.minRepls.label', default: 'Min Repls')}" />
							
								<g:sortableColumn property="closedCollectionName" title="${message(code: 'preservationSystem.closedCollectionName.label', default: 'Closed Collection Name')}" />
							
								<g:sortableColumn property="openCollectionName" title="${message(code: 'preservationSystem.openCollectionName.label', default: 'Open Collection Name')}" />
							
								<g:sortableColumn property="presServer" title="${message(code: 'preservationSystem.presServer.label', default: 'Pres Server')}" />
							
								<g:sortableColumn property="urisAggr" title="${message(code: 'preservationSystem.urisAggr.label', default: 'Uris Aggr')}" />
							
							</tr>
						</thead>
						<tbody>
							<g:each in="${preservationSystemInstanceList}" status="i" var="preservationSystemInstance">
								<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
								
									<td><g:link action="show" id="${preservationSystemInstance.id}">${fieldValue(bean: preservationSystemInstance, field: "minRepls")}</g:link></td>
								
									<td>${fieldValue(bean: preservationSystemInstance, field: "closedCollectionName")}</td>
								
									<td>${fieldValue(bean: preservationSystemInstance, field: "openCollectionName")}</td>
								
									<td>${fieldValue(bean: preservationSystemInstance, field: "presServer")}</td>
								
									<td>${fieldValue(bean: preservationSystemInstance, field: "urisAggr")}</td>
								
								</tr>
							</g:each>
						</tbody>
					</table>
				</div>
				<div class="pagination">
					<g:paginate total="${preservationSystemInstanceCount ?: 0}" />
				</div>
			</div>
		</div>
	</body>
</html>
