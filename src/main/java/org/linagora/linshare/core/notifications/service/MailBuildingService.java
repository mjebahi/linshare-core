/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2015. Contribute to
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

package org.linagora.linshare.core.notifications.service;

import java.util.List;

import org.linagora.linshare.core.domain.constants.Language;
import org.linagora.linshare.core.domain.constants.MailContentType;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.Entry;
import org.linagora.linshare.core.domain.entities.MailConfig;
import org.linagora.linshare.core.domain.entities.ShareEntryGroup;
import org.linagora.linshare.core.domain.entities.UploadProposition;
import org.linagora.linshare.core.domain.entities.UploadRequest;
import org.linagora.linshare.core.domain.entities.UploadRequestEntry;
import org.linagora.linshare.core.domain.entities.UploadRequestUrl;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.objects.MailContainerWithRecipient;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.notifications.context.EmailContext;
import org.linagora.linshare.core.notifications.dto.ContextMetadata;

public interface MailBuildingService {

	MailContainerWithRecipient build(EmailContext context) throws BusinessException;

	boolean fakeBuildIsSupported(MailContentType type) throws BusinessException;

	MailContainerWithRecipient fakeBuild(MailContentType type, MailConfig cfg, Language language, Integer flavor) throws BusinessException;

	List<ContextMetadata> getAvailableVariables(MailContentType type);

	MailContainerWithRecipient buildSharedDocUpdated(Entry shareEntry,
			String oldDocName, long size) throws BusinessException;

	MailContainerWithRecipient buildSharedDocDeleted(Account actor,
			Entry shareEntry) throws BusinessException;

	MailContainerWithRecipient buildSharedDocUpcomingOutdated(
			Entry shareEntry, Integer days) throws BusinessException;

	MailContainerWithRecipient buildDocUpcomingOutdated(DocumentEntry document,
			Integer days) throws BusinessException;

	MailContainerWithRecipient buildCreateUploadProposition(User recipient, UploadProposition proposition)
			throws BusinessException;

	MailContainerWithRecipient buildRejectUploadProposition(User sender, UploadProposition proposition)
			throws BusinessException;

	MailContainerWithRecipient buildUpdateUploadRequest(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildActivateUploadRequest(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildFilterUploadRequest(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildCreateUploadRequest(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildAckDeleteFileUploadRequest(User owner, UploadRequestUrl request, UploadRequestEntry entry)
			throws BusinessException;

	MailContainerWithRecipient buildRemindUploadRequest(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildUploadRequestBeforeExpiryWarnOwner(User owner, UploadRequest request)
			throws BusinessException;

	MailContainerWithRecipient buildUploadRequestBeforeExpiryWarnRecipient(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildUploadRequestExpiryWarnOwner(User owner, UploadRequest request)
			throws BusinessException;

	MailContainerWithRecipient buildUploadRequestExpiryWarnRecipient(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildCloseUploadRequestByRecipient(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildCloseUploadRequestByOwner(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildDeleteUploadRequestByOwner(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildErrorUploadRequestNoSpaceLeft(User owner, UploadRequestUrl request)
			throws BusinessException;

	MailContainerWithRecipient buildNoDocumentHasBeenDownloadedAcknowledgement(
			ShareEntryGroup shareEntryGroup)
					throws BusinessException;
}
