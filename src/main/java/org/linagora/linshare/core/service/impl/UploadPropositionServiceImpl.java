/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015-2018 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2018. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */

package org.linagora.linshare.core.service.impl;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.business.service.DomainBusinessService;
import org.linagora.linshare.core.business.service.UploadPropositionBusinessService;
import org.linagora.linshare.core.domain.constants.LinShareConstants;
import org.linagora.linshare.core.domain.constants.UploadPropositionStatus;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.BooleanValueFunctionality;
import org.linagora.linshare.core.domain.entities.Contact;
import org.linagora.linshare.core.domain.entities.FileSizeUnitClass;
import org.linagora.linshare.core.domain.entities.IntegerValueFunctionality;
import org.linagora.linshare.core.domain.entities.LanguageEnumValueFunctionality;
import org.linagora.linshare.core.domain.entities.UploadPropositionOLD;
import org.linagora.linshare.core.domain.entities.UploadRequest;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.objects.MailContainerWithRecipient;
import org.linagora.linshare.core.domain.objects.SizeUnitValueFunctionality;
import org.linagora.linshare.core.domain.objects.TimeUnitValueFunctionality;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.notifications.service.MailBuildingService;
import org.linagora.linshare.core.rac.UploadPropositionResourceAccessControl;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;
import org.linagora.linshare.core.service.NotifierService;
import org.linagora.linshare.core.service.UploadPropositionService;
import org.linagora.linshare.core.service.UploadRequestGroupService;
import org.linagora.linshare.core.service.UserService;
import org.linagora.linshare.mongo.entities.UploadProposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class UploadPropositionServiceImpl  extends GenericServiceImpl<Account, UploadProposition> implements UploadPropositionService {

	private static final Logger logger = LoggerFactory
			.getLogger(UploadPropositionServiceImpl.class);

	private final UploadPropositionBusinessService uploadPropositionBusinessService;

	private final DomainBusinessService domainBusinessService;

	private final UserService userService;

	private final FunctionalityReadOnlyService functionalityReadOnlyService;

	private final MailBuildingService mailBuildingService;

	private final NotifierService notifierService;
	
	private final UploadRequestGroupService uploadRequestGroupService;

	public UploadPropositionServiceImpl(
			final UploadPropositionBusinessService uploadPropositionBusinessService,
			final UploadPropositionResourceAccessControl rac,
			final DomainBusinessService domainBusinessService,
			final UserService userService,
			final FunctionalityReadOnlyService functionalityReadOnlyService,
			final MailBuildingService mailBuildingService,
			final NotifierService notifierService,
			final UploadRequestGroupService uploadRequestGroupService) {
		super(rac);
		this.uploadPropositionBusinessService = uploadPropositionBusinessService;
		this.domainBusinessService = domainBusinessService;
		this.userService = userService;
		this.functionalityReadOnlyService = functionalityReadOnlyService;
		this.mailBuildingService = mailBuildingService;
		this.notifierService = notifierService;
		this.uploadRequestGroupService = uploadRequestGroupService;
	}

	@Override
	public UploadProposition create(Account authUser, String recipientMail, UploadProposition uploadProposition) {
		Validate.notNull(uploadProposition, "The Upload proposition cannot be null");
		AbstractDomain rootDomain = domainBusinessService.getUniqueRootDomain();
		Account targetedAccount = userService.findOrCreateUser(recipientMail, rootDomain.getUuid());
		preChecks(authUser, targetedAccount);
		checkCreatePermission(authUser, targetedAccount, UploadProposition.class,
				BusinessErrorCode.UPLOAD_PROPOSITION_CAN_NOT_CREATE, null);
		if (UploadPropositionStatus.SYSTEM_REJECTED.equals(uploadProposition.getStatus())) {
			// The uploadProposition has been rejected by the system : no upload request nor
			// upload proposition are created
			return uploadProposition;
		}
		if (Strings.isNullOrEmpty(uploadProposition.getAccountUuid())) {
			uploadProposition.setAccountUuid(targetedAccount.getLsUuid());
		}
		if (Strings.isNullOrEmpty(uploadProposition.getDomainUuid())) {
			uploadProposition.setAccountUuid(rootDomain.getUuid());
		}
		UploadProposition created;
		if (UploadPropositionStatus.SYSTEM_ACCEPTED.equals(uploadProposition.getStatus())) {
			// The uploadProposition has been accepted by the system : an upload request is
			// directly created
			acceptHook((User) targetedAccount, uploadProposition);
			created = uploadProposition;
		} else {
			// No system rules have been applied to the proposition : an upload proposition
			// is submitted to the targetted account
			created = uploadPropositionBusinessService.create(uploadProposition);
			MailContainerWithRecipient mail = mailBuildingService.buildCreateUploadProposition((User) targetedAccount,
					uploadProposition);
			notifierService.sendNotification(mail);
		}
		// TODO Audit
		return created;
	}

	@Override
	public void delete(Account actor, UploadPropositionOLD prop)
			throws BusinessException {
		Validate.notNull(actor, "Actor must be set.");
		uploadPropositionBusinessService.delete(prop);
	}

	@Override
	public UploadPropositionOLD find(Account actor, String uuid)
			throws BusinessException {
		Validate.notNull(actor, "Actor must be set.");
		return uploadPropositionBusinessService.findByUuid(uuid);
	}

	@Override
	public List<UploadPropositionOLD> findAll(User actor) throws BusinessException {
		Validate.notNull(actor, "Actor must be set.");
		return uploadPropositionBusinessService.findAllByMail(actor.getMail());
	}

	@Override
	public void checkIfValidRecipient(Account actor, String mail,
			String domainId) throws BusinessException {
		Validate.notNull(actor, "Actor must be set.");
		Validate.notEmpty(mail, "Mail must be set.");
		if (!actor.hasUploadPropositionRole()) {
			logger.equals(actor.getAccountRepresentation()
					+ " is using an unauthorized api");
			throw new BusinessException(BusinessErrorCode.FORBIDDEN,
					"You are not authorized to use this method.");
		}
		if (domainId == null) {
			domainId = LinShareConstants.rootDomainIdentifier;
		}
		try {
			userService.findOrCreateUserWithDomainPolicies(mail, domainId);
		} catch (BusinessException ex) {
			throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND,
					"Recipient not found.");
		}
	}

	@Override
	public void accept(User actor, UploadPropositionOLD e)
			throws BusinessException {
		logger.debug("Accepting proposition: " + e.getUuid());
		e.setStatus(UploadPropositionStatus.USER_ACCEPTED);
		e = uploadPropositionBusinessService.update(e);
		//TODO acceptHook
		//acceptHook(actor, e);
	}

	@Override
	public void reject(User actor, UploadPropositionOLD e)
			throws BusinessException {
		logger.debug("Rejecting proposition: " + e.getUuid());
		e.setStatus(UploadPropositionStatus.USER_REJECTED);
		uploadPropositionBusinessService.update(e);
		MailContainerWithRecipient mail = mailBuildingService
				.buildRejectUploadProposition(actor, e);
		notifierService.sendNotification(mail);
	}

	public void acceptHook(User owner, UploadProposition created)
			throws BusinessException {
		UploadRequest req = new UploadRequest();
		req.setUploadPropositionRequestUuid(created.getUuid());
		getDefaultValue(owner, req);// get value default from domain
		Contact contact = new Contact(created.getContact().getMail());
		uploadRequestGroupService.create(owner, owner, req, Lists.newArrayList(contact),
				created.getLabel(), created.getBody(), null);
	}

	public void getDefaultValue(User owner, UploadRequest req)
			throws BusinessException {
		AbstractDomain domain = owner.getDomain();

		TimeUnitValueFunctionality expiryDateFunc = functionalityReadOnlyService
				.getUploadRequestExpiryTimeFunctionality(domain);

		if (expiryDateFunc.getActivationPolicy().getStatus()) {
			logger.debug("expiryDateFunc is activated");
			if (expiryDateFunc.getDelegationPolicy() != null
					&& expiryDateFunc.getDelegationPolicy().getStatus()) {
				logger.debug("expiryDateFunc has a delegation policy");
			}
			Calendar c = Calendar.getInstance();
			c.add(expiryDateFunc.toCalendarValue(),
					expiryDateFunc.getValue());
			req.setExpiryDate(c.getTime());
		}

		SizeUnitValueFunctionality maxDepositSizeFunc = functionalityReadOnlyService
				.getUploadRequestMaxDepositSizeFunctionality(domain);

		if (maxDepositSizeFunc.getActivationPolicy().getStatus()) {
			logger.debug("maxDepositSizeFunc is activated");
			if (maxDepositSizeFunc.getDelegationPolicy() != null
					&& maxDepositSizeFunc.getDelegationPolicy().getStatus()) {
				logger.debug("maxDepositSizeFunc has a delegation policy");
			}
			long maxDepositSize = ((FileSizeUnitClass) maxDepositSizeFunc
					.getUnit()).getPlainSize(maxDepositSizeFunc.getValue());
			req.setMaxDepositSize(maxDepositSize);
		}

		IntegerValueFunctionality maxFileCountFunc = functionalityReadOnlyService
				.getUploadRequestMaxFileCountFunctionality(domain);

		if (maxFileCountFunc.getActivationPolicy().getStatus()) {
			logger.debug("maxFileCountFunc is activated");
			if (maxFileCountFunc.getDelegationPolicy() != null
					&& maxFileCountFunc.getDelegationPolicy().getStatus()) {
				logger.debug("maxFileCountFunc has a delegation policy");
			}
			int maxFileCount = maxFileCountFunc.getValue();
			req.setMaxFileCount(maxFileCount);
		}

		SizeUnitValueFunctionality maxFileSizeFunc = functionalityReadOnlyService
				.getUploadRequestMaxFileSizeFunctionality(domain);

		if (maxFileSizeFunc.getActivationPolicy().getStatus()) {
			logger.debug("maxFileSizeFunc is activated");
			if (maxFileSizeFunc.getDelegationPolicy() != null
					&& maxFileSizeFunc.getDelegationPolicy().getStatus()) {
				logger.debug("maxFileSizeFunc has a delegation policy");
			}
			long maxFileSize = ((FileSizeUnitClass) maxFileSizeFunc.getUnit())
					.getPlainSize(maxFileSizeFunc.getValue());
			req.setMaxFileSize(maxFileSize);
		}

		LanguageEnumValueFunctionality notificationLangFunc = functionalityReadOnlyService
				.getUploadRequestNotificationLanguageFunctionality(domain);

		if (notificationLangFunc.getActivationPolicy().getStatus()) {
			logger.debug("notificationLangFunc is activated");
			if (notificationLangFunc.getDelegationPolicy() != null
					&& notificationLangFunc.getDelegationPolicy().getStatus()) {
				logger.debug("notificationLangFunc has a delegation policy");
			}
			String locale = notificationLangFunc.getValue().getTapestryLocale();
			req.setLocale(locale);
		}

		BooleanValueFunctionality secureUrlFunc = functionalityReadOnlyService
				.getUploadRequestSecureUrlFunctionality(domain);

		if (secureUrlFunc.getActivationPolicy().getStatus()) {
			logger.debug("secureUrlFunc is activated");
			if (secureUrlFunc.getDelegationPolicy() != null
					&& secureUrlFunc.getDelegationPolicy().getStatus()) {
				logger.debug("secureUrlFunc has a delegation policy");
			}
			req.setSecured(secureUrlFunc.getValue());
		}

		BooleanValueFunctionality canDeleteFunc = functionalityReadOnlyService
				.getUploadRequestCandDeleteFileFunctionality(domain);

		if (canDeleteFunc.getActivationPolicy().getStatus()) {
			logger.debug("depositFunc is activated");
			if (canDeleteFunc.getDelegationPolicy() != null
					&& canDeleteFunc.getDelegationPolicy().getStatus()) {
				logger.debug("depositFunc has a delegation policy");
			}
			req.setCanDelete(canDeleteFunc.getValue());
		}

		BooleanValueFunctionality canCloseFunc = functionalityReadOnlyService
				.getUploadRequestCanCloseFunctionality(domain);

		if (canCloseFunc.getActivationPolicy().getStatus()) {
			logger.debug("canCloseFunc  is activated");
			if (canCloseFunc.getDelegationPolicy() != null
					&& canCloseFunc.getDelegationPolicy().getStatus()) {
				logger.debug("canCloseFunc  has a delegation policy");
			}
			req.setCanClose(canCloseFunc.getValue());
		}
	}
}
