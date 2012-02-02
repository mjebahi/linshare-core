/*
 *    This file is part of Linshare.
 *
 *   Linshare is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   Linshare is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public
 *   License along with Foobar.  If not, see
 *                                    <http://www.gnu.org/licenses/>.
 *
 *   (c) 2008 Groupe Linagora - http://linagora.org
 *
*/
package org.linagora.linShare.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linagora.linShare.core.domain.constants.GroupMembershipStatus;
import org.linagora.linShare.core.domain.constants.Language;
import org.linagora.linShare.core.domain.constants.MailSubjectEnum;
import org.linagora.linShare.core.domain.constants.MailTemplateEnum;
import org.linagora.linShare.core.domain.constants.UserType;
import org.linagora.linShare.core.domain.entities.AbstractDomain;
import org.linagora.linShare.core.domain.entities.Contact;
import org.linagora.linShare.core.domain.entities.Document;
import org.linagora.linShare.core.domain.entities.Group;
import org.linagora.linShare.core.domain.entities.GroupMember;
import org.linagora.linShare.core.domain.entities.Guest;
import org.linagora.linShare.core.domain.entities.MailContainer;
import org.linagora.linShare.core.domain.entities.MailContainerWithRecipient;
import org.linagora.linShare.core.domain.entities.MailSubject;
import org.linagora.linShare.core.domain.entities.MailTemplate;
import org.linagora.linShare.core.domain.entities.SecuredUrl;
import org.linagora.linShare.core.domain.entities.Share;
import org.linagora.linShare.core.domain.entities.User;
import org.linagora.linShare.core.domain.vo.DocumentVo;
import org.linagora.linShare.core.domain.vo.ShareDocumentVo;
import org.linagora.linShare.core.exception.BusinessException;
import org.linagora.linShare.core.exception.TechnicalErrorCode;
import org.linagora.linShare.core.exception.TechnicalException;
import org.linagora.linShare.core.service.MailContentBuildingService;


public class MailContentBuildingServiceImpl implements MailContentBuildingService {
	private final String pUrlBase;
	private final String pUrlInternal;
	private final String mailContentTxt;
	private final String mailContentHTML;
	private final String mailContentHTMLWithoutLogo;
	private final boolean displayLogo;
	
    private final static Log logger = LogFactory.getLog(MailContentBuildingServiceImpl.class);

	
	public MailContentBuildingServiceImpl(final String urlBase, 
			final String urlInternal, final String mailContentTxt,
			final String mailContentHTML, final String mailContentHTMLWithoutLogo,
			final boolean displayLogo) throws BusinessException {
		this.pUrlBase = urlBase;
		this.pUrlInternal = urlInternal;
        this.mailContentTxt = mailContentTxt;
        this.mailContentHTML = mailContentHTML;
        this.mailContentHTMLWithoutLogo = mailContentHTMLWithoutLogo;
        this.displayLogo = displayLogo;
	}

	/**
	 * Retrieve the mail subject from config.
	 * 
	 * @param language the language of the email
	 * @param mailSubject the enum key
	 * @return the MailSubject object
	 * @throws BusinessException when no mail subject was found for the given enum key
	 */
	private MailSubject getMailSubject(User actor, Language language, MailSubjectEnum mailSubject) throws BusinessException {
		AbstractDomain domain = actor.getDomain();
		Set<MailSubject> subjects = domain.getMessagesConfiguration().getMailSubjects();
		
		for (MailSubject mailSubject_ : subjects) {
			if (mailSubject_.getLanguage().equals(language) && mailSubject_.getMailSubject().equals(mailSubject)) {
				return new MailSubject(mailSubject_);
			}
		}

		logger.error("Bad mail subject "+ mailSubject.name() +" for language " + language.name());
		throw new TechnicalException(TechnicalErrorCode.MAIL_EXCEPTION,"Bad mail subject "+ mailSubject.name() +" for language " + language.name());
	}

	/**
	 * Retrieve the mail template from config.
	 * 
	 * @param language the language of the email
	 * @param mailTemplate the enum key
	 * @return the MailTemplate object
	 * @throws BusinessException when no mail template was found for the given enum key
	 */
	private MailTemplate getMailTemplate(User actor, Language language, MailTemplateEnum mailTemplate) throws BusinessException {
		Set<MailTemplate> templates = actor.getDomain().getMessagesConfiguration().getMailTemplates();
		
		for (MailTemplate mailTemplate_ : templates) {
			if (mailTemplate_.getLanguage().equals(language) && mailTemplate_.getMailTemplate().equals(mailTemplate)) {
				return new MailTemplate(mailTemplate_);
			}
		}

		logger.error("Bad mail template "+ mailTemplate.name() +" for language " + language.name());
		throw new TechnicalException(TechnicalErrorCode.MAIL_EXCEPTION,"Bad mail template "+ mailTemplate.name() +" for language " + language.name());
	}
	
	/**
	 * Build the final mail content.
	 * 
	 * @param mailContainer
	 * @param subject
	 * @param bodyTXT
	 * @param bodyHTML
	 * @param recipient
	 * @param owner
	 * @param personalMessage
	 * @throws BusinessException
	 */
	private void buildMailContainerSetProperties(User actor, MailContainer mailContainer, String subject, String bodyTXT, String bodyHTML, User recipient, User owner, String personalMessage) throws BusinessException {
		MailTemplate greetings = buildTemplateGreetings(actor, mailContainer.getLanguage(), recipient);
		MailTemplate footer = buildTemplateFooter(actor, mailContainer.getLanguage());
		String contentTXT = this.mailContentTxt;
		String contentHTML = null;
		if (displayLogo) {
			contentHTML = this.mailContentHTML;
		} else {
			contentHTML = this.mailContentHTMLWithoutLogo;
		}
		
		if (personalMessage != null && personalMessage.trim().length() > 0) {
			MailTemplate personalMessageTemplate = buildTemplatePersonalMessage(actor, mailContainer.getLanguage(), owner, personalMessage);

			contentTXT = StringUtils.replace(contentTXT, "${personalMessage}", personalMessageTemplate.getContentTXT());
	        contentHTML = StringUtils.replace(contentHTML, "${personalMessage}", personalMessageTemplate.getContentHTML());
			contentTXT = StringUtils.replace(contentTXT, "%{personalMessage}", personalMessageTemplate.getContentTXT());
	        contentHTML = StringUtils.replace(contentHTML, "%{personalMessage}", personalMessageTemplate.getContentHTML());
		}
		else {
			contentTXT = StringUtils.replace(contentTXT, "${personalMessage}", "");
	        contentHTML = StringUtils.replace(contentHTML, "${personalMessage}", "");
			contentTXT = StringUtils.replace(contentTXT, "%{personalMessage}", "");
	        contentHTML = StringUtils.replace(contentHTML, "%{personalMessage}", "");
		}
		
        contentTXT = StringUtils.replace(contentTXT, "${greetings}", greetings.getContentTXT());
        contentTXT = StringUtils.replace(contentTXT, "${footer}", footer.getContentTXT());
        contentTXT = StringUtils.replace(contentTXT, "${body}", bodyTXT);
        contentHTML = StringUtils.replace(contentHTML, "${greetings}", greetings.getContentHTML());
        contentHTML = StringUtils.replace(contentHTML, "${footer}", footer.getContentHTML());
        contentHTML = StringUtils.replace(contentHTML, "${body}", bodyHTML);
        contentHTML = StringUtils.replace(contentHTML, "${mailSubject}", subject);
        contentTXT = StringUtils.replace(contentTXT, "%{greetings}", greetings.getContentTXT());
        contentTXT = StringUtils.replace(contentTXT, "%{footer}", footer.getContentTXT());
        contentTXT = StringUtils.replace(contentTXT, "%{body}", bodyTXT);
        contentHTML = StringUtils.replace(contentHTML, "%{greetings}", greetings.getContentHTML());
        contentHTML = StringUtils.replace(contentHTML, "%{footer}", footer.getContentHTML());
        contentHTML = StringUtils.replace(contentHTML, "%{body}", bodyHTML);
        contentHTML = StringUtils.replace(contentHTML, "%{mailSubject}", subject);
        
        mailContainer.setContentTXT(contentTXT);
        mailContainer.setContentHTML(contentHTML);
        mailContainer.setSubject(subject);
        	
	}
	
	/**
	 * Build the final mail content.
	 * 
	 * @param mailContainer
	 * @param subject
	 * @param bodyTXT
	 * @param bodyHTML
	 * @param recipient
	 * @param owner
	 * @param personalMessage
	 * @throws BusinessException
	 */
	private MailContainer buildMailContainer(User actor, final MailContainer mailContainerInitial, String subject, String bodyTXT, String bodyHTML, User recipient, User owner, String personalMessage) throws BusinessException {
		MailContainer mailContainer = new MailContainer(mailContainerInitial);
		buildMailContainerSetProperties(actor, mailContainer, subject,  bodyTXT,  bodyHTML,  recipient,  owner,  personalMessage);
		return mailContainer;
	}
	
	/**
	 * Build the final mail content with recipient.
	 * 
	 * @param mailContainer
	 * @param subject
	 * @param bodyTXT
	 * @param bodyHTML
	 * @param recipient
	 * @param owner
	 * @param personalMessage
	 * @throws BusinessException
	 */
	private MailContainerWithRecipient buildMailContainerWithRecipient(User actor, final MailContainer mailContainerInitial, String subject, String bodyTXT, String bodyHTML, User recipient, User owner, String personalMessage) throws BusinessException {
		MailContainerWithRecipient mailContainer = new MailContainerWithRecipient(mailContainerInitial,recipient.getMail());
        buildMailContainerSetProperties(actor, mailContainer, subject,  bodyTXT,  bodyHTML,  recipient,  owner,  personalMessage);;
        return mailContainer;
	}	
	
	
	/**
	 * Template GREETINGS
	 */
	private MailTemplate buildTemplateGreetings(User actor, Language language, User user) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.GREETINGS);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		contentTXT = StringUtils.replace(contentTXT, "${firstName}", user.getFirstName());
        contentTXT = StringUtils.replace(contentTXT, "${lastName}", user.getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", user.getFirstName());
        contentHTML = StringUtils.replace(contentHTML, "${lastName}", user.getLastName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template FOOTER
	 */
	private MailTemplate buildTemplateFooter(User actor, Language language) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.FOOTER);
        
        return template;
	}
	
	/**
	 * Template CONFIRM_DOWNLOAD_ANONYMOUS
	 */
	private MailTemplate buildTemplateConfirmDownloadAnonymous(User actor, Language language, List<Document> docs, String email) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.CONFIRM_DOWNLOAD_ANONYMOUS);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		StringBuffer names = new StringBuffer();
		StringBuffer namesTxt = new StringBuffer();
		if (docs != null && docs.size()>0) {
			for (Document doc : docs) {
				names.append("<li>"+doc.getName()+"</li>");
				namesTxt.append(doc.getName()+"\n");
			}	
		}

		contentTXT = StringUtils.replace(contentTXT, "${email}", email);
        contentTXT = StringUtils.replace(contentTXT, "${documentNamesTxt}", namesTxt.toString());
        contentHTML = StringUtils.replace(contentHTML, "${email}", email);
        contentHTML = StringUtils.replace(contentHTML, "${documentNames}", names.toString());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template CONFIRM_DOWNLOAD_REGISTERED
	 */
	private MailTemplate buildTemplateConfirmDownloadRegistered(User actor, Language language, List<Document> docs, User recipient) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.CONFIRM_DOWNLOAD_REGISTERED);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		StringBuffer names = new StringBuffer();
		StringBuffer namesTxt = new StringBuffer();
		if (docs != null && docs.size()>0) {
			for (Document doc : docs) {
				names.append("<li>"+doc.getName()+"</li>");
				namesTxt.append(doc.getName()+"\n");
			}	
		}

		contentTXT = StringUtils.replace(contentTXT, "${recipientFirstName}", recipient.getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${recipientLastName}", recipient.getLastName());
        contentTXT = StringUtils.replace(contentTXT, "${documentNamesTxt}", namesTxt.toString());
        contentHTML = StringUtils.replace(contentHTML, "${recipientFirstName}", recipient.getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${recipientLastName}", recipient.getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${documentNames}", names.toString());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template DECRYPT_URL
	 */
	private MailTemplate buildTemplateDecryptUrl(User actor, Language language, boolean hasToBeFilled, String linShareUrlAnonymous, String jwsEncryptUrl) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.DECRYPT_URL);
		
		if(hasToBeFilled){
			String contentTXT = template.getContentTXT();
			String contentHTML = template.getContentHTML();
			
			contentTXT = StringUtils.replace(contentTXT, "${jwsEncryptUrl}", jwsEncryptUrl);
	        contentHTML = StringUtils.replace(contentHTML, "${jwsEncryptUrl}", jwsEncryptUrl);
			
	        template.setContentTXT(contentTXT);
	        template.setContentHTML(contentHTML);
		} else {
	        template.setContentTXT("");
	        template.setContentHTML("");
		}
		
		return template;
	}
	
	/**
	 * Template PERSONAL_MESSAGE
	 */
	private MailTemplate buildTemplatePersonalMessage(User actor, Language language, User owner, String message) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.PERSONAL_MESSAGE);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		contentTXT = StringUtils.replace(contentTXT, "${ownerFirstName}", owner.getFirstName());
        contentTXT = StringUtils.replace(contentTXT, "${ownerLastName}", owner.getLastName());
        contentTXT = StringUtils.replace(contentTXT, "${message}", message);
        contentHTML = StringUtils.replace(contentHTML, "${ownerFirstName}", owner.getFirstName());
        contentHTML = StringUtils.replace(contentHTML, "${ownerLastName}", owner.getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${message}", message);
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template GUEST_INVITATION
	 */
	private MailTemplate buildTemplateGuestInvitation(User actor, Language language, User owner) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.GUEST_INVITATION);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		contentTXT = StringUtils.replace(contentTXT, "${ownerFirstName}", owner.getFirstName());
        contentTXT = StringUtils.replace(contentTXT, "${ownerLastName}", owner.getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${ownerFirstName}", owner.getFirstName());
        contentHTML = StringUtils.replace(contentHTML, "${ownerLastName}", owner.getLastName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template LINSHARE_URL
	 */
	private MailTemplate buildTemplateLinshareURL(User actor, Language language, String linShareUrl) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.LINSHARE_URL);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		contentTXT = StringUtils.replace(contentTXT, "${url}", linShareUrl);
        contentHTML = StringUtils.replace(contentHTML, "${url}", linShareUrl);
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template ACCOUNT_DESCRIPTION
	 */
	private MailTemplate buildTemplateAccountDescription(User actor, Language language, User guest, String password) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.ACCOUNT_DESCRIPTION);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		contentTXT = StringUtils.replace(contentTXT, "${mail}", guest.getMail());
		contentTXT = StringUtils.replace(contentTXT, "${password}", password);
        contentHTML = StringUtils.replace(contentHTML, "${mail}", guest.getMail());
        contentHTML = StringUtils.replace(contentHTML, "${password}", password);
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template SHARE_NOTIFICATION
	 */
	private MailTemplate buildTemplateShareNotification(User actor, Language language, List<DocumentVo> docs, User owner) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.SHARE_NOTIFICATION);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		StringBuffer names = new StringBuffer();
		StringBuffer namesTxt = new StringBuffer();
		if (docs != null && docs.size()>0) {
			for (DocumentVo doc : docs) {
				names.append("<li>"+doc.getFileName()+"</li>");
				namesTxt.append(doc.getFileName()+"\n");
			}	
		}
		
		String number = new Integer(docs.size()).toString();

		contentTXT = StringUtils.replace(contentTXT, "${firstName}", owner.getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${lastName}", owner.getLastName());
		contentTXT = StringUtils.replace(contentTXT, "${number}", number);
        contentTXT = StringUtils.replace(contentTXT, "${documentNamesTxt}", namesTxt.toString());
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", owner.getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${lastName}", owner.getLastName());
		contentHTML = StringUtils.replace(contentHTML, "${number}", number);
        contentHTML = StringUtils.replace(contentHTML, "${documentNames}", names.toString());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template FILE_DOWNLOAD_URL
	 */
	private MailTemplate buildTemplateFileDownloadURL(User actor, Language language, String url, String urlParam) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.FILE_DOWNLOAD_URL);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		contentTXT = StringUtils.replace(contentTXT, "${url}", url);
		contentTXT = StringUtils.replace(contentTXT, "${urlparam}", urlParam);
        contentHTML = StringUtils.replace(contentHTML, "${url}", url);
        contentHTML = StringUtils.replace(contentHTML, "${urlparam}", urlParam);
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template PASSWORD_GIVING
	 */
	private MailTemplate buildTemplatePasswordGiving(User actor, Language language, String password) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.PASSWORD_GIVING);
		
		if (password != null && password.trim().length() > 0) {
			String contentTXT = template.getContentTXT();
			String contentHTML = template.getContentHTML();

			contentTXT = StringUtils.replace(contentTXT, "${password}", password);
	        contentHTML = StringUtils.replace(contentHTML, "${password}", password);

	        template.setContentTXT(contentTXT);
	        template.setContentHTML(contentHTML);
		}
		else {

	        template.setContentTXT("");
	        template.setContentHTML("");
		}
		
		return template;
	}
	
	/**
	 * Template FILE_UPDATED
	 */
	private MailTemplate buildTemplateFileUpdated(User actor, Language language, User owner, Document document, String oldDocName, String fileSizeTxt) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.FILE_UPDATED);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		contentTXT = StringUtils.replace(contentTXT, "${firstName}", owner.getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${lastName}", owner.getLastName());
		contentTXT = StringUtils.replace(contentTXT, "${fileOldName}", oldDocName);
        contentTXT = StringUtils.replace(contentTXT, "${fileName}", document.getName());
		contentTXT = StringUtils.replace(contentTXT, "${fileSize}", fileSizeTxt);
        contentTXT = StringUtils.replace(contentTXT, "${mimeType}", document.getType());
        
        
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", owner.getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${lastName}", owner.getLastName());
		contentHTML = StringUtils.replace(contentHTML, "${fileOldName}", oldDocName);
        contentHTML = StringUtils.replace(contentHTML, "${fileName}", document.getName());
        contentHTML = StringUtils.replace(contentHTML, "${fileSize}", fileSizeTxt);
        contentHTML = StringUtils.replace(contentHTML, "${mimeType}", document.getType());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template GROUP_SHARE_NOTIFICATION
	 */
	private MailTemplate buildTemplateGroupShareNotification(User actor, Language language, List<ShareDocumentVo> docs, User owner, Group group) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.GROUP_SHARE_NOTIFICATION);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();

		StringBuffer names = new StringBuffer();
		StringBuffer namesTxt = new StringBuffer();
		if (docs != null && docs.size()>0) {
			for (DocumentVo doc : docs) {
				names.append("<li>"+doc.getFileName()+"</li>");
				namesTxt.append(doc.getFileName()+"\n");
			}	
		}
		
		String number = new Integer(docs.size()).toString();

		contentTXT = StringUtils.replace(contentTXT, "${firstName}", owner.getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${lastName}", owner.getLastName());
		contentTXT = StringUtils.replace(contentTXT, "${number}", number);
        contentTXT = StringUtils.replace(contentTXT, "${documentNamesTxt}", namesTxt.toString());
        contentTXT = StringUtils.replace(contentTXT, "${groupName}", group.getName());
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", owner.getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${lastName}", owner.getLastName());
		contentHTML = StringUtils.replace(contentHTML, "${number}", number);
        contentHTML = StringUtils.replace(contentHTML, "${documentNames}", names.toString());
        contentHTML = StringUtils.replace(contentHTML, "${groupName}", group.getName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template GROUP_MEMBERSHIP_STATUS
	 */
	private MailTemplate buildTemplateGroupMembershipStatus(User actor, MailContainer mailContainer, GroupMember newMember, Group group, GroupMembershipStatus status) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, mailContainer.getLanguage(), MailTemplateEnum.GROUP_MEMBERSHIP_STATUS);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();
		
		String statusString = mailContainer.getData("GroupMembershipStatus."+status.toString());
		
		contentTXT = StringUtils.replace(contentTXT, "${newMemberFirstName}", newMember.getUser().getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${newMemberLastName}", newMember.getUser().getLastName());
        contentTXT = StringUtils.replace(contentTXT, "${status}", statusString);
        contentTXT = StringUtils.replace(contentTXT, "${groupName}", group.getName());
        contentHTML = StringUtils.replace(contentHTML, "${newMemberFirstName}", newMember.getUser().getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${newMemberLastName}", newMember.getUser().getLastName());
		contentHTML = StringUtils.replace(contentHTML, "${status}", statusString);
        contentHTML = StringUtils.replace(contentHTML, "${groupName}", group.getName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template GROUP_NEW_MEMBER
	 */
	private MailTemplate buildTemplateGroupNewMember(User actor, 
			MailContainer mailContainer, GroupMember newMember, Group group) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, mailContainer.getLanguage(), MailTemplateEnum.GROUP_NEW_MEMBER);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();
		
		
        contentTXT = StringUtils.replace(contentTXT, "${groupName}", group.getName());
        contentHTML = StringUtils.replace(contentHTML, "${groupName}", group.getName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/**
	 * Template GROUP_SHARE_DELETED
	 */
	private MailTemplate buildTemplateGroupSharingDeleted(User actor, Language language, Document doc, User manager, Group group) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.GROUP_SHARE_DELETED);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();
		
		contentTXT = StringUtils.replace(contentTXT, "${firstName}", manager.getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${lastName}", manager.getLastName());
        contentTXT = StringUtils.replace(contentTXT, "${groupName}", group.getName());
        contentTXT = StringUtils.replace(contentTXT, "${documentName}", doc.getName());
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", manager.getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${lastName}", manager.getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${groupName}", group.getName());
        contentHTML = StringUtils.replace(contentHTML, "${documentName}", doc.getName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}

	/**
	 * Template SHARED_FILE_DELETED
	 */
	private MailTemplate buildTemplateSharedFileDeleted(User actor, Language language, Document doc, User owner) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.SHARED_FILE_DELETED);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();
		
		contentTXT = StringUtils.replace(contentTXT, "${firstName}", owner.getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${lastName}", owner.getLastName());
        contentTXT = StringUtils.replace(contentTXT, "${documentName}", doc.getName());
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", owner.getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${lastName}", owner.getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${documentName}", doc.getName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}

	/**
	 * Template SECURED_URL_UPCOMING_OUTDATED
	 */
	private MailTemplate buildTemplateUpcomingOutdatedSecuredUrl(User actor, 
			Language language, SecuredUrl securedUrl, Contact recipient,
			Integer days) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.SECURED_URL_UPCOMING_OUTDATED);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();
		
		contentTXT = StringUtils.replace(contentTXT, "${firstName}", securedUrl.getSender().getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${lastName}", securedUrl.getSender().getLastName());
        contentTXT = StringUtils.replace(contentTXT, "${nbDays}", days.toString());
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", securedUrl.getSender().getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${lastName}", securedUrl.getSender().getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${nbDays}", days.toString());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}


	/**
	 * Template SHARED_DOC_UPCOMING_OUTDATED
	 */
	private MailTemplate buildTemplateUpcomingOutdatedShare(User actor, Language language,
			Share share, Integer days) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.SHARED_DOC_UPCOMING_OUTDATED);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();
		
		contentTXT = StringUtils.replace(contentTXT, "${firstName}", share.getSender().getFirstName());
		contentTXT = StringUtils.replace(contentTXT, "${lastName}", share.getSender().getLastName());
        contentTXT = StringUtils.replace(contentTXT, "${nbDays}", days.toString());
        contentTXT = StringUtils.replace(contentTXT, "${documentName}", share.getDocument().getName());
        contentHTML = StringUtils.replace(contentHTML, "${firstName}", share.getSender().getFirstName());
		contentHTML = StringUtils.replace(contentHTML, "${lastName}", share.getSender().getLastName());
        contentHTML = StringUtils.replace(contentHTML, "${nbDays}", days.toString());
        contentHTML = StringUtils.replace(contentHTML, "${documentName}", share.getDocument().getName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}

	/**
	 * Template DOC_UPCOMING_OUTDATED
	 */
	private MailTemplate buildTemplateUpcomingOutdatedFile(User actor, Language language,
			Document document, Integer days) throws BusinessException {
		MailTemplate template = getMailTemplate(actor, language, MailTemplateEnum.DOC_UPCOMING_OUTDATED);
		String contentTXT = template.getContentTXT();
		String contentHTML = template.getContentHTML();
		
        contentTXT = StringUtils.replace(contentTXT, "${nbDays}", days.toString());
        contentTXT = StringUtils.replace(contentTXT, "${documentName}", document.getName());
        contentHTML = StringUtils.replace(contentHTML, "${nbDays}", days.toString());
        contentHTML = StringUtils.replace(contentHTML, "${documentName}", document.getName());
        
        template.setContentTXT(contentTXT);
        template.setContentHTML(contentHTML);
        
        return template;
	}
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.impl.MailContentBuildingService#buildMailAnonymousDownload(org.linagora.linShare.core.domain.entities.MailContainer, java.util.List, java.lang.String, org.linagora.linShare.core.domain.entities.User)
	 */
	public MailContainer buildMailAnonymousDownload(User actor, MailContainer mailContainer, List<Document> docs, String email, User recipient) throws BusinessException {
		MailTemplate template = buildTemplateConfirmDownloadAnonymous(actor, mailContainer.getLanguage(), docs, email);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.ANONYMOUS_DOWNLOAD);
		return buildMailContainer(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null);
	}
	
	public MailContainerWithRecipient buildMailAnonymousDownloadWithRecipient(User actor, MailContainer mailContainer, List<Document> docs, String email, User recipient) throws BusinessException {
		MailTemplate template = buildTemplateConfirmDownloadAnonymous(actor, mailContainer.getLanguage(), docs, email);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.ANONYMOUS_DOWNLOAD);
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null);
	}
	
	public List<MailContainerWithRecipient> buildMailAnonymousDownloadWithOneRecipient(User actor, MailContainer mailContainer, List<Document> docs, String email, User recipient) throws BusinessException {
		MailTemplate template = buildTemplateConfirmDownloadAnonymous(actor, mailContainer.getLanguage(), docs, email);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.ANONYMOUS_DOWNLOAD);
		
		
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();
		mailContainerWithRecipient.add(buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null));
		return mailContainerWithRecipient;
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.impl.MailContentBuildingService#buildMailRegisteredDownload(org.linagora.linShare.core.domain.entities.MailContainer, java.util.List, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.User)
	 */
	public MailContainer buildMailRegisteredDownload(User actor, MailContainer mailContainer, List<Document> docs, User downloadingUser, User recipient) throws BusinessException {
		MailTemplate template = buildTemplateConfirmDownloadRegistered(actor, mailContainer.getLanguage(), docs, downloadingUser);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.REGISTERED_DOWNLOAD);
		return buildMailContainer(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null);
	}
	
	public MailContainerWithRecipient buildMailRegisteredDownloadWithRecipient(User actor, MailContainer mailContainer, List<Document> docs, User downloadingUser, User recipient) throws BusinessException {
		MailTemplate template = buildTemplateConfirmDownloadRegistered(actor, mailContainer.getLanguage(), docs, downloadingUser);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.REGISTERED_DOWNLOAD);
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null);
	}	
	
	public List<MailContainerWithRecipient> buildMailRegisteredDownloadWithOneRecipient(User actor, MailContainer mailContainer, List<Document> docs, User downloadingUser, User recipient) throws BusinessException {
		MailTemplate template = buildTemplateConfirmDownloadRegistered(actor, mailContainer.getLanguage(), docs, downloadingUser);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.REGISTERED_DOWNLOAD);
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();
		mailContainerWithRecipient.add(buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null));
		return mailContainerWithRecipient;
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.impl.MailContentBuildingService#buildMailNewGuest(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.User, java.lang.String)
	 */
	public MailContainer buildMailNewGuest(User actor, MailContainer mailContainer, User owner, User recipient, String password) throws BusinessException {
		MailTemplate template1 = buildTemplateGuestInvitation(actor, mailContainer.getLanguage(), owner);
		MailTemplate template2 = buildTemplateLinshareURL(actor, mailContainer.getLanguage(), pUrlBase);
		MailTemplate template3 = buildTemplateAccountDescription(actor, mailContainer.getLanguage(), recipient, password);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_GUEST);
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentTXT.append(template3.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		contentHTML.append(template3.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), recipient, owner, mailContainer.getPersonalMessage());
	}
	
	public MailContainerWithRecipient buildMailNewGuestWithRecipient(User actor, MailContainer mailContainer, User owner, User recipient, String password) throws BusinessException {
		MailTemplate template1 = buildTemplateGuestInvitation(actor, mailContainer.getLanguage(), owner);
		MailTemplate template2 = buildTemplateLinshareURL(actor, mailContainer.getLanguage(), pUrlBase);
		MailTemplate template3 = buildTemplateAccountDescription(actor, mailContainer.getLanguage(), recipient, password);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_GUEST);
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentTXT.append(template3.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		contentHTML.append(template3.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), recipient, owner, mailContainer.getPersonalMessage());
	}	
	
	public List<MailContainerWithRecipient> buildMailNewGuestWithOneRecipient(User actor, MailContainer mailContainer, User owner, User recipient, String password) throws BusinessException {
		MailTemplate template1 = buildTemplateGuestInvitation(actor, mailContainer.getLanguage(), owner);
		MailTemplate template2 = buildTemplateLinshareURL(actor, mailContainer.getLanguage(), pUrlBase);
		MailTemplate template3 = buildTemplateAccountDescription(actor, mailContainer.getLanguage(), recipient, password);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_GUEST);
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentTXT.append(template3.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		contentHTML.append(template3.getContentHTML() + "<br />");
		
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();
		
		mailContainerWithRecipient.add(buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), recipient, owner, mailContainer.getPersonalMessage()));
		
		return mailContainerWithRecipient;
	}		
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.impl.MailContentBuildingService#buildMailResetPassword(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, java.lang.String)
	 */
	public MailContainer buildMailResetPassword(User actor, MailContainer mailContainer, User recipient, String password) throws BusinessException {
		MailTemplate template = buildTemplateAccountDescription(actor, mailContainer.getLanguage(), recipient, password);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.RESET_PASSWORD);
		
		return buildMailContainer(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null);
	}
	
	public MailContainerWithRecipient buildMailResetPasswordWithRecipient(User actor, MailContainer mailContainer, User recipient, String password) throws BusinessException {
		MailTemplate template = buildTemplateAccountDescription(actor, mailContainer.getLanguage(), recipient, password);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.RESET_PASSWORD);
		
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null);
	}	
	
	public List<MailContainerWithRecipient> buildMailResetPasswordWithOneRecipient(User actor, MailContainer mailContainer, User recipient, String password) throws BusinessException {
		MailTemplate template = buildTemplateAccountDescription(actor, mailContainer.getLanguage(), recipient, password);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.RESET_PASSWORD);
		
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();
		
		mailContainerWithRecipient.add(buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), template.getContentTXT(), template.getContentHTML(), recipient, null, null));
		
		return mailContainerWithRecipient;
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.impl.MailContentBuildingService#buildMailNewSharing(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.User, java.util.List, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public MailContainer buildMailNewSharing(User actor, MailContainer mailContainer, User owner, User recipient, List<DocumentVo> docs, String linShareUrl, String linShareUrlParam, String password, boolean hasToDecrypt, String jwsEncryptUrl) throws BusinessException {
		MailTemplate template1 = buildTemplateShareNotification(actor, mailContainer.getLanguage(), docs, owner);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, linShareUrlParam);
		MailTemplate template3 = buildTemplateDecryptUrl(actor, mailContainer.getLanguage(), hasToDecrypt, linShareUrl, jwsEncryptUrl);
		MailTemplate template4 = buildTemplatePasswordGiving(actor, mailContainer.getLanguage(), password);
		
		String subjectContent = mailContainer.getSubject();
		if (subjectContent == null || subjectContent.length() < 1) {
			MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_SHARING);
			subjectContent = subject.getContent();
		}
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentTXT.append(template3.getContentTXT() + "\n");
		contentTXT.append(template4.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		contentHTML.append(template3.getContentHTML() + "<br />");
		contentHTML.append(template4.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subjectContent, contentTXT.toString(), contentHTML.toString(), recipient, owner, mailContainer.getPersonalMessage());
	}
	
	public MailContainerWithRecipient buildMailNewSharingWithRecipient(User actor, MailContainer mailContainer, User owner, User recipient, List<DocumentVo> docs, String linShareUrl, String linShareUrlParam, String password, boolean hasToDecrypt, String jwsEncryptUrl) throws BusinessException {
		MailTemplate template1 = buildTemplateShareNotification(actor, mailContainer.getLanguage(), docs, owner);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, linShareUrlParam);
		MailTemplate template3 = buildTemplateDecryptUrl(actor, mailContainer.getLanguage(), hasToDecrypt, linShareUrl, jwsEncryptUrl);
		MailTemplate template4 = buildTemplatePasswordGiving(actor, mailContainer.getLanguage(), password);
		
		String subjectContent = mailContainer.getSubject();
		if (subjectContent == null || subjectContent.length() < 1) {
			MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_SHARING);
			subjectContent = subject.getContent();
		}
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentTXT.append(template3.getContentTXT() + "\n");
		contentTXT.append(template4.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		contentHTML.append(template3.getContentHTML() + "<br />");
		contentHTML.append(template4.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subjectContent, contentTXT.toString(), contentHTML.toString(), recipient, owner, mailContainer.getPersonalMessage());
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailNewSharing(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, java.lang.String, java.util.List, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public MailContainer buildMailNewSharing(User actor, MailContainer mailContainer,
			User owner, String recipientMail, List<DocumentVo> docs,
			String linShareUrl, String linShareUrlParam, String password, 
			boolean hasToDecrypt, String jwsEncryptUrl)
			throws BusinessException {
		User tempUser = new Guest(recipientMail, recipientMail, "", recipientMail);
		return buildMailNewSharing(actor, mailContainer, owner, tempUser, docs, linShareUrl, linShareUrlParam, password, hasToDecrypt, jwsEncryptUrl);
	}

	public MailContainerWithRecipient buildMailNewSharingWithRecipient(User actor, MailContainer mailContainer,
			User owner, String recipientMail, List<DocumentVo> docs,
			String linShareUrl, String linShareUrlParam, String password, 
			boolean hasToDecrypt, String jwsEncryptUrl)
			throws BusinessException {
		User tempUser = new Guest(recipientMail, recipientMail, "", recipientMail);
		return buildMailNewSharingWithRecipient(actor, mailContainer, owner, tempUser, docs, linShareUrl, linShareUrlParam, password, hasToDecrypt, jwsEncryptUrl);
	}
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailSharedDocUpdated(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, java.lang.String, org.linagora.linShare.core.domain.entities.Document, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public MailContainer buildMailSharedDocUpdated(User actor, MailContainer mailContainer,
			User owner, String recipientMail, Document document,
			String oldDocName, String fileSizeTxt, String linShareUrl,
			String linShareUrlParam)
			throws BusinessException {
		User tempUser = new Guest(recipientMail, recipientMail, "", recipientMail);
		return buildMailSharedDocUpdated(actor, mailContainer, owner, tempUser, document, oldDocName, fileSizeTxt, linShareUrl, linShareUrlParam);
	}

	public MailContainerWithRecipient buildMailSharedDocUpdatedWithRecipient(User actor, MailContainer mailContainer,
			User owner, String recipientMail, Document document,
			String oldDocName, String fileSizeTxt, String linShareUrl,
			String linShareUrlParam)
			throws BusinessException {
		User tempUser = new Guest(recipientMail, recipientMail, "", recipientMail);
		return buildMailSharedDocUpdatedWithRecipient(actor, mailContainer, owner, tempUser, document, oldDocName, fileSizeTxt, linShareUrl, linShareUrlParam);
	}
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailSharedDocUpdated(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.Document, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public MailContainer buildMailSharedDocUpdated(User actor, MailContainer mailContainer, 
			User owner, User recipient, Document document, 
			String oldDocName, String fileSizeTxt, 
			String linShareUrl, String linShareUrlParam) 
			throws BusinessException {
		MailTemplate template1 = buildTemplateFileUpdated(actor, mailContainer.getLanguage(), owner, document, oldDocName, fileSizeTxt);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, linShareUrlParam);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_UPDATED);
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), recipient, null, null);
	}
	
	public MailContainerWithRecipient buildMailSharedDocUpdatedWithRecipient(User actor, MailContainer mailContainer, 
			User owner, User recipient, Document document, 
			String oldDocName, String fileSizeTxt, 
			String linShareUrl, String linShareUrlParam) 
			throws BusinessException {
		MailTemplate template1 = buildTemplateFileUpdated(actor, mailContainer.getLanguage(), owner, document, oldDocName, fileSizeTxt);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, linShareUrlParam);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_UPDATED);
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), recipient, null, null);
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailNewGroupSharing(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.Group, java.util.List, java.lang.String, java.lang.String)
	 */
	public MailContainer buildMailNewGroupSharing(User actor, MailContainer mailContainer,
			User owner, Group group, List<ShareDocumentVo> docs, String linShareUrl,
			String linShareUrlParam, boolean isOneDocEncrypted, String jwsEncryptUrlString) throws BusinessException {
		User tempUser = new Guest(group.getFunctionalEmail(), group.getFunctionalEmail(), "", group.getFunctionalEmail());
		return buildMailNewGroupSharing(actor, mailContainer, owner, tempUser, group, docs, linShareUrl, linShareUrlParam, isOneDocEncrypted, jwsEncryptUrlString);
	}
	
	public MailContainerWithRecipient buildMailNewGroupSharingWithRecipient(User actor, MailContainer mailContainer,
			User owner, Group group, List<ShareDocumentVo> docs, String linShareUrl,
			String linShareUrlParam, boolean isOneDocEncrypted, String jwsEncryptUrlString) throws BusinessException {
		User tempUser = new Guest(group.getFunctionalEmail(), group.getFunctionalEmail(), "", group.getFunctionalEmail());
		return buildMailNewGroupSharingWithRecipient(actor, mailContainer, owner, tempUser, group, docs, linShareUrl, linShareUrlParam, isOneDocEncrypted, jwsEncryptUrlString);
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailNewGroupSharing(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.Group, java.util.List, java.lang.String, java.lang.String)
	 */
	public MailContainer buildMailNewGroupSharing(User actor, MailContainer mailContainer,
			User owner, User recipient, Group group, List<ShareDocumentVo> docs,
			String linShareUrl, String linShareUrlParam,
			boolean isOneDocEncrypted, String jwsEncryptUrlString)
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupShareNotification(actor, mailContainer.getLanguage(), docs, owner, group);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, linShareUrlParam);
		MailTemplate template3 = buildTemplateDecryptUrl(actor, mailContainer.getLanguage(), isOneDocEncrypted, linShareUrl, jwsEncryptUrlString);
		
		String subjectContent = mailContainer.getSubject();
		if (subjectContent == null || subjectContent.length() < 1) {
			MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_GROUP_SHARING);
			subjectContent = subject.getContent();
		}
		subjectContent = StringUtils.replace(subjectContent, "${groupName}", group.getName());
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentTXT.append(template3.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		contentHTML.append(template3.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subjectContent, contentTXT.toString(), contentHTML.toString(), recipient, owner, mailContainer.getPersonalMessage());
	}
	
	public MailContainerWithRecipient buildMailNewGroupSharingWithRecipient(User actor, MailContainer mailContainer,
			User owner, User recipient, Group group, List<ShareDocumentVo> docs,
			String linShareUrl, String linShareUrlParam,
			boolean isOneDocEncrypted, String jwsEncryptUrlString)
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupShareNotification(actor, mailContainer.getLanguage(), docs, owner, group);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, linShareUrlParam);
		MailTemplate template3 = buildTemplateDecryptUrl(actor, mailContainer.getLanguage(), isOneDocEncrypted, linShareUrl, jwsEncryptUrlString);
		
		String subjectContent = mailContainer.getSubject();
		if (subjectContent == null || subjectContent.length() < 1) {
			MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_GROUP_SHARING);
			subjectContent = subject.getContent();
		}
		subjectContent = StringUtils.replace(subjectContent, "${groupName}", group.getName());
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentTXT.append(template3.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		contentHTML.append(template3.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subjectContent, contentTXT.toString(), contentHTML.toString(), recipient, owner, mailContainer.getPersonalMessage());
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailGroupSharingDeleted(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.Group, org.linagora.linShare.core.domain.entities.Document)
	 */
	public MailContainer buildMailGroupSharingDeleted(User actor, 
			MailContainer mailContainer, User manager, Group group, Document doc)
			throws BusinessException {
		User tempUser = new Guest(group.getFunctionalEmail(), group.getFunctionalEmail(), "", group.getFunctionalEmail());
		return buildMailGroupSharingDeleted(actor, mailContainer, manager, tempUser, group, doc);
	}
	
	public MailContainerWithRecipient buildMailGroupSharingDeletedWithRecipient(User actor, 
			MailContainer mailContainer, User manager, Group group, Document doc)
			throws BusinessException {
		User tempUser = new Guest(group.getFunctionalEmail(), group.getFunctionalEmail(), "", group.getFunctionalEmail());
		return buildMailGroupSharingDeletedWithRecipient(actor, mailContainer, manager, tempUser, group, doc);
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailGroupSharingDeleted(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.User, org.linagora.linShare.core.domain.entities.Group, org.linagora.linShare.core.domain.entities.Document)
	 */
	public MailContainer buildMailGroupSharingDeleted(User actor, 
			MailContainer mailContainer, User manager, User user, Group group, Document doc)
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupSharingDeleted(actor, mailContainer.getLanguage(), doc, manager, group);
		
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.GROUP_SHARING_DELETED);
		String subjectContent = StringUtils.replace(subject.getContent(), "${groupName}", group.getName());
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subjectContent, contentTXT.toString(), contentHTML.toString(), user, null, null);
	}
	
	public MailContainerWithRecipient buildMailGroupSharingDeletedWithRecipient(User actor, 
			MailContainer mailContainer, User manager, User user, Group group, Document doc)
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupSharingDeleted(actor, mailContainer.getLanguage(), doc, manager, group);
		
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.GROUP_SHARING_DELETED);
		String subjectContent = StringUtils.replace(subject.getContent(), "${groupName}", group.getName());
		
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subjectContent, contentTXT.toString(), contentHTML.toString(), user, null, null);
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailGroupMembershipStatus(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.GroupMember, org.linagora.linShare.core.domain.entities.Group, org.linagora.linShare.core.domain.entities.GroupMembershipStatus)
	 */
	public MailContainer buildMailGroupMembershipStatus(User actor, 
			MailContainer mailContainer, GroupMember newMember, Group group, GroupMembershipStatus status)
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupMembershipStatus(actor, mailContainer, newMember, group, status);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.MEMBERSHIP_REQUEST_STATUS);

		String subjectString = StringUtils.replace(subject.getContent(), "${newMemberFirstName}", newMember.getUser().getFirstName());
		subjectString = StringUtils.replace(subjectString, "${newMemberLastName}", newMember.getUser().getLastName());
		subjectString = StringUtils.replace(subjectString, "${groupName}", group.getName());
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subjectString, contentTXT.toString(), contentHTML.toString(), newMember.getOwner(), null, null);
	}
	
	public MailContainerWithRecipient buildMailGroupMembershipStatusWithRecipient(User actor, 
			MailContainer mailContainer, GroupMember newMember, Group group, GroupMembershipStatus status)
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupMembershipStatus(actor, mailContainer, newMember, group, status);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.MEMBERSHIP_REQUEST_STATUS);

		String subjectString = StringUtils.replace(subject.getContent(), "${newMemberFirstName}", newMember.getUser().getFirstName());
		subjectString = StringUtils.replace(subjectString, "${newMemberLastName}", newMember.getUser().getLastName());
		subjectString = StringUtils.replace(subjectString, "${groupName}", group.getName());
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subjectString, contentTXT.toString(), contentHTML.toString(), newMember.getOwner(), null, null);
	}	
	
	public List<MailContainerWithRecipient> buildMailGroupMembershipStatusWithOneRecipient(User actor, 
			MailContainer mailContainer, GroupMember newMember, Group group, GroupMembershipStatus status)
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupMembershipStatus(actor, mailContainer, newMember, group, status);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.MEMBERSHIP_REQUEST_STATUS);

		String subjectString = StringUtils.replace(subject.getContent(), "${newMemberFirstName}", newMember.getUser().getFirstName());
		subjectString = StringUtils.replace(subjectString, "${newMemberLastName}", newMember.getUser().getLastName());
		subjectString = StringUtils.replace(subjectString, "${groupName}", group.getName());
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();

		
		mailContainerWithRecipient.add(buildMailContainerWithRecipient(actor, mailContainer, subjectString, contentTXT.toString(), contentHTML.toString(), newMember.getOwner(), null, null));
		
		return mailContainerWithRecipient;
	}	
	
	/* (non-Javadoc)
	 * @see org.linagora.linShare.core.service.MailContentBuildingService#buildMailNewGroupMember(org.linagora.linShare.core.domain.entities.MailContainer, org.linagora.linShare.core.domain.entities.GroupMember, org.linagora.linShare.core.domain.entities.Group)
	 */
	public MailContainer buildMailNewGroupMember(User actor, 
			MailContainer mailContainer, GroupMember newMember, Group group) 
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupNewMember(actor, mailContainer, newMember, group);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_GROUP_MEMBER);
		
		String subjectString = StringUtils.replace(subject.getContent(), "${groupName}", group.getName());
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subjectString, contentTXT.toString(), contentHTML.toString(), newMember.getUser(), null, null);
	}
	
	public MailContainerWithRecipient buildMailNewGroupMemberWithRecipient(User actor, 
			MailContainer mailContainer, GroupMember newMember, Group group) 
			throws BusinessException {
		MailTemplate template1 = buildTemplateGroupNewMember(actor, mailContainer, newMember, group);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.NEW_GROUP_MEMBER);
		
		String subjectString = StringUtils.replace(subject.getContent(), "${groupName}", group.getName());
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subjectString, contentTXT.toString(), contentHTML.toString(), newMember.getUser(), null, null);
	}	
	
	public MailContainer buildMailSharedFileDeleted(User actor, 
			MailContainer mailContainer, Document doc, User owner,
			Contact receiver)
			throws BusinessException {
		User tempUser = new Guest(receiver.getMail(), receiver.getMail(), "", receiver.getMail());
		return buildMailSharedFileDeleted(actor, mailContainer, doc, owner, tempUser);
	}
	
	public MailContainerWithRecipient buildMailSharedFileDeletedWithRecipient(User actor, 
			MailContainer mailContainer, Document doc, User owner,
			Contact receiver)
			throws BusinessException {
		User tempUser = new Guest(receiver.getMail(), receiver.getMail(), "", receiver.getMail());
		return buildMailSharedFileDeletedWithRecipient(actor, mailContainer, doc, owner, tempUser);
	}	
	
	public MailContainer buildMailSharedFileDeleted(User actor, 
			MailContainer mailContainer, Document doc, User owner, User receiver) 
			throws BusinessException {

		MailTemplate template1 = buildTemplateSharedFileDeleted(actor, mailContainer.getLanguage(), doc, owner);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_DELETED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), receiver, null, null);
	}
	
	
	public MailContainerWithRecipient buildMailSharedFileDeletedWithRecipient(User actor, 
			MailContainer mailContainer, Document doc, User owner, User receiver) 
			throws BusinessException {

		MailTemplate template1 = buildTemplateSharedFileDeleted(actor, mailContainer.getLanguage(), doc, owner);
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_DELETED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), receiver, null, null);
	}	
	
	
	public MailContainer buildMailUpcomingOutdatedSecuredUrl(User actor, 
			MailContainer mailContainer, SecuredUrl securedUrl,
			Contact recipient, Integer days, String securedUrlWithParam) throws BusinessException {

		MailTemplate template1 = buildTemplateUpcomingOutdatedSecuredUrl(actor, mailContainer.getLanguage(), securedUrl, recipient, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), securedUrlWithParam, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		User tempUser = new Guest(recipient.getMail(), recipient.getMail(), "", recipient.getMail());
		
		return buildMailContainer(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), tempUser, null, null);
	}
	
	public MailContainerWithRecipient buildMailUpcomingOutdatedSecuredUrlWithRecipient(User actor, 
			MailContainer mailContainer, SecuredUrl securedUrl,
			Contact recipient, Integer days, String securedUrlWithParam) throws BusinessException {

		MailTemplate template1 = buildTemplateUpcomingOutdatedSecuredUrl(actor, mailContainer.getLanguage(), securedUrl, recipient, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), securedUrlWithParam, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		User tempUser = new Guest(recipient.getMail(), recipient.getMail(), "", recipient.getMail());
		
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), tempUser, null, null);
	}	

	public MailContainer buildMailUpcomingOutdatedShare(User actor, 
			MailContainer mailContainer, Share share, Integer days) throws BusinessException {
		
		String linShareUrl = share.getReceiver().getUserType().equals(UserType.INTERNAL) ? this.pUrlInternal : this.pUrlBase;

		MailTemplate template1 = buildTemplateUpcomingOutdatedShare(actor, mailContainer.getLanguage(), share, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), share.getReceiver(), null, null);

	}
	
	public MailContainerWithRecipient buildMailUpcomingOutdatedShareWithRecipient(User actor, 
			MailContainer mailContainer, Share share, Integer days) throws BusinessException {
		
		String linShareUrl = share.getReceiver().getUserType().equals(UserType.INTERNAL) ? this.pUrlInternal : this.pUrlBase;

		MailTemplate template1 = buildTemplateUpcomingOutdatedShare(actor, mailContainer.getLanguage(), share, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), share.getReceiver(), null, null);

	}	
	
	public List<MailContainerWithRecipient> buildMailUpcomingOutdatedShareWithOneRecipient(User actor, 
			MailContainer mailContainer, Share share, Integer days) throws BusinessException {
		
		String linShareUrl = share.getReceiver().getUserType().equals(UserType.INTERNAL) ? this.pUrlInternal : this.pUrlBase;

		MailTemplate template1 = buildTemplateUpcomingOutdatedShare(actor, mailContainer.getLanguage(), share, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.SHARED_DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();

		mailContainerWithRecipient.add(buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), share.getReceiver(), null, null));
		
		return mailContainerWithRecipient;

	}		
	
	public MailContainer buildMailUpcomingOutdatedDocument(User actor, 
			MailContainer mailContainer, Document document, Integer days)
			throws BusinessException {
		
		String linShareUrl = document.getOwner().getUserType().equals(UserType.INTERNAL) ? this.pUrlInternal : this.pUrlBase;

		MailTemplate template1 = buildTemplateUpcomingOutdatedFile(actor, mailContainer.getLanguage(), document, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		return buildMailContainer(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), document.getOwner(), null, null);

	}
	
	public MailContainerWithRecipient buildMailUpcomingOutdatedDocumentWithRecipient(User actor, 
			MailContainer mailContainer, Document document, Integer days)
			throws BusinessException {
		
		String linShareUrl = document.getOwner().getUserType().equals(UserType.INTERNAL) ? this.pUrlInternal : this.pUrlBase;

		MailTemplate template1 = buildTemplateUpcomingOutdatedFile(actor, mailContainer.getLanguage(), document, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		return buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), document.getOwner(), null, null);

	}
	
	public List<MailContainerWithRecipient> buildMailUpcomingOutdatedDocumentWithOneRecipient(User actor, 
			MailContainer mailContainer, Document document, Integer days)
			throws BusinessException {
		
		String linShareUrl = document.getOwner().getUserType().equals(UserType.INTERNAL) ? this.pUrlInternal : this.pUrlBase;

		MailTemplate template1 = buildTemplateUpcomingOutdatedFile(actor, mailContainer.getLanguage(), document, days);
		MailTemplate template2 = buildTemplateFileDownloadURL(actor, mailContainer.getLanguage(), linShareUrl, "");
		MailSubject subject = getMailSubject(actor, mailContainer.getLanguage(), MailSubjectEnum.DOC_UPCOMING_OUTDATED);
        
		StringBuffer contentTXT = new StringBuffer();
		StringBuffer contentHTML = new StringBuffer();
		contentTXT.append(template1.getContentTXT() + "\n");
		contentTXT.append(template2.getContentTXT() + "\n");
		contentHTML.append(template1.getContentHTML() + "<br />");
		contentHTML.append(template2.getContentHTML() + "<br />");
		
		List<MailContainerWithRecipient> mailContainerWithRecipient = new ArrayList<MailContainerWithRecipient>();
		mailContainerWithRecipient.add(buildMailContainerWithRecipient(actor, mailContainer, subject.getContent(), contentTXT.toString(), contentHTML.toString(), document.getOwner(), null, null));
		
		return mailContainerWithRecipient;

	}	
	
}
