package daweb3
/*
 DA-NRW Software Suite | ContentBroker

 Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
 Universität zu Köln

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
*/

/**
 * Lists the SIP being converted or DIP being retrieved and their respective status 
 * @Author Jens Peters
 * @Author Sebastian Cuy 
 */
import java.util.logging.Logger;
import grails.plugin.springsecurity.annotation.Secured
import org.hibernate.criterion.CriteriaSpecification;

import org.springframework.aop.TrueClassFilter;
import org.springframework.dao.DataIntegrityViolationException

class QueueEntryController {

	
	def springSecurityService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
		
		def contractorList
		def cbNodeList = CbNode.list()
		User user = springSecurityService.currentUser
		def admin = 0;
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}
		if (admin == 1) {	
			contractorList = User.list()
		} else {
			contractorList = User.findAll("from User as c where c.shortName=:csn",
	        [csn: user.getShortName()])
		}
		
		[contractorList:contractorList,
		cbNodeList:cbNodeList]
		// different List View per Role
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
		render(view:"adminList", model:[contractorList:contractorList,
		cbNodeList:cbNodeList]);
		}
	}
    

    def listSnippet() {
		User us = springSecurityService.currentUser
    	def queueEntries = null	
		def periodical = true;	
		def contractorList = User.list()
		def admin = 0;
		if (us.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}

		if (!params.search){	
			if (admin != 1) {	
				queueEntries = QueueEntry.findAll("from QueueEntry as q where q.obj.user.shortName=:csn",
	             [csn: us.getShortName()])
				
			} else {
				admin = 1;
				queueEntries = QueueEntry.findAll("from QueueEntry as q")
				
			}
			[queueEntryInstanceList: queueEntries,
				admin:admin, periodical:periodical,
				contractorList:contractorList ]
			if (us.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
					render(view:"adminListSnippet", model:[queueEntryInstanceList: queueEntries, admin:admin, periodical:periodical, contractorList:contractorList]);
			} else render(view:"listSnippet", model:[queueEntryInstanceList: queueEntries, admin:admin, periodical:periodical, contractorList:contractorList]);
			
		} else {
			
			periodical = false;
			def c = QueueEntry.createCriteria()
			queueEntries = c.list() {
				if (params.search?.obj) params.search.obj.each { key, value ->
						if (value!="") {
						projections {
							obj {
								like(key, "%" + value + "%")
							}
						}
					}
				}
				if (params.search?.initialNode) { 
					if (params.search?.initialNode !="null"){ 
						eq("initialNode", params.search.initialNode)
					}
				}
				if (params.search?.status) {
					like("status", params.search.status+"%")
				}
				
				if (admin==0) {
					projections {
						obj {
								
								user {
									eq("shortName", us.getShortName())								
								}
						}
					}
				} else {
		
				if (params.search?.user){
					if(params.search?.user !="null"){
						projections {
							obj {
									user {
										eq("shortName", params.search.user)
									}
								}
							}	
						}
					}
				}
			}
			
		}
		if (us.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
				render(view:"adminListSnippet", model:[queueEntryInstanceList: queueEntries, admin:admin, periodical:periodical, contractorList:contractorList]);
		} else render(view:"listSnippet", model:[queueEntryInstanceList: queueEntries, admin:admin, periodical:periodical, contractorList:contractorList]);
    }
	
	/** 
	 * Generates detailed view for one item (SIP) in workflow
	 */
    def show() {
        def queueEntryInstance = QueueEntry.get(params.id)
        if (!queueEntryInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'queueEntry.label', default: 'QueueEntry'), params.id])
            redirect(action: "list")
            return
        }

        [queueEntryInstance: queueEntryInstance]
    }
	
	/**
	 * Applies button and functionality to retry the last workflow step for an item
	 */
	@Secured(['ROLE_NODEADMIN'])
	def queueRetry() {
		def queueEntryInstance = QueueEntry.get(params.id)
		if (queueEntryInstance) {
			def status = queueEntryInstance.getStatus()
			def newstat = status.substring(0,status.length()-1)
			newstat = newstat + "0"
			queueEntryInstance.status = newstat
			queueEntryInstance.modified = Math.round(new Date().getTime()/1000L)
			if( !queueEntryInstance.save() ) {
				log.error("Validation errors on save")
				queueEntryInstance.errors.each {
					log.error(it)
				}
			} 
			flash.message = "Status zurückgesetzt!" 			
			redirect(action: "list")
			return
		} else flash.message = message(code: 'default.not.found.message', args: [message(code: 'queueEntry.label', default: 'QueueEntry'), params.id])
		redirect(action: "list")
		return

		[queueEntryInstance: queueEntryInstance]
	}
	
	/**
	 * Applies button and functionality to recover all the workflow for an item
	 */
	@Secured(['ROLE_NODEADMIN'])
	def queueRecover() {
		def queueEntryInstance = QueueEntry.get(params.id)
		if (queueEntryInstance) {
			def status = queueEntryInstance.getStatus()
			int state = status.toInteger();
			
			if ((state>=123 && state<=353) && status.endsWith("3") && !status.endsWith("1")) {
				// Recover state is 600
				def newstat = "600"
				queueEntryInstance.status = newstat
				queueEntryInstance.modified = Math.round(new Date().getTime()/1000L)
				if( !queueEntryInstance.save() ) {
					log.error("Validation errors on save")
					queueEntryInstance.errors.each {
						log.error(it)
					}
				}
				flash.message = "Paket recovered!"
			} else flash.message = "Paket ist nicht zurückstellbar"
			redirect(action: "list")
			return
			
		} else flash.message = message(code: 'default.not.found.message', args: [message(code: 'queueEntry.label', default: 'QueueEntry'), params.id])
		redirect(action: "list")
		return

		[queueEntryInstance: queueEntryInstance]
	}
	
	/**
	 * Applies button and functionality to remove an item from ContentBroker workflow
	 */
	@Secured(['ROLE_NODEADMIN'])
	def queueDelete() {
		def queueEntryInstance = QueueEntry.get(params.id)
		if (queueEntryInstance) {
			def status = queueEntryInstance.getStatus()
			int state = status.toInteger();
			
			if (queueEntryInstance.showDeletionButton() && state <401) {
				// Delete state is 800
				def newstat = "800"
				queueEntryInstance.status = newstat
				queueEntryInstance.modified = Math.round(new Date().getTime()/1000L)
				if( !queueEntryInstance.save() ) {
					log.error("Validation errors on save")
					queueEntryInstance.errors.each {
						log.error(it)
					}
				}
				flash.message = "Paket für Löschung vorgesehen"
			} else flash.message = "Paket ist nicht löschbar, wenden Sie sich an Ihren Knotenadmin!"
			redirect(action: "list")
			return
			
		} else flash.message = message(code: 'default.not.found.message', args: [message(code: 'queueEntry.label', default: 'QueueEntry'), params.id])
		redirect(action: "list")
		return

		[queueEntryInstance: queueEntryInstance]
	}
	
	def listMigrationRequests () {
		User user = springSecurityService.currentUser
		def queueEntries
		def admin = 0;
		if (user.authorities.any { it.authority == "ROLE_NODEADMIN" }) {
			admin = 1;
		}
		if (params.search==null){
			if (admin != 1) {
				queueEntries = QueueEntry.findAll("from QueueEntry as q where q.obj.user.shortName=:csn and q.status='640'",
				 [csn: user.shortName])
			} else {
				admin = true;
				queueEntries = QueueEntry.findAll("from QueueEntry as q where q.status='640'")
				
			}
			[queueEntryInstanceList: queueEntries,
				admin:admin ]
	}
	}
	/**
	 * Applies status and functionality to answer with yes on migration requests
	 */
	def performMigrationRequestYes() {
		def queueEntryInstance = QueueEntry.get(params.id)
		if (queueEntryInstance) {
			def status = queueEntryInstance.getStatus()
			int state = status.toInteger();
				def newstat = "640"
				queueEntryInstance.status = newstat
				queueEntryInstance.answer = "YES"
				queueEntryInstance.modified = Math.round(new Date().getTime()/1000L)
				if( !queueEntryInstance.save() ) {
					log.error("Validation errors on save")
					queueEntryInstance.errors.each {
						log.error(it)
					}
				}
				flash.message = "Antwort Ja"
			} else flash.message = "Nachfrage konnte nicht beantwortet werden- Fehler"
			redirect(action: "listMigrationRequests")
			return
		redirect(action: "listMigrationRequests")
		return

		[queueEntryInstance: queueEntryInstance]
	}
	
	/**
	 * Applies status and functionality to answer with yes on migration requests
	 */
	def performMigrationRequestNo() {
		def queueEntryInstance = QueueEntry.get(params.id)
		if (queueEntryInstance) {
			def status = queueEntryInstance.getStatus()
			int state = status.toInteger();
				def newstat = "640"
				queueEntryInstance.status = newstat
				queueEntryInstance.answer = "NO"
				queueEntryInstance.modified = Math.round(new Date().getTime()/1000L)
				if( !queueEntryInstance.save() ) {
					log.error("Validation errors on save")
					queueEntryInstance.errors.each {
						log.error(it)
					}
				}
				flash.message = "Antwort Nein"
			} else flash.message = "Nachfrage konnte nicht beantwortet werden- Fehler"
			redirect(action: "listMigrationRequests")
			return
		redirect(action: "listMigrationRequests")
		return

		[queueEntryInstance: queueEntryInstance]
	}

}
