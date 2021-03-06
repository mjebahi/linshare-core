/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2018 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009-2018. Contribute to
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
package org.linagora.linshare.core.facade.webservice.user.impl;

import java.util.List;

import org.jsoup.helper.Validate;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.user.UploadPropositionFacade;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.UploadPropositionService;
import org.linagora.linshare.mongo.entities.UploadProposition;

public class UploadPropositionFacadeImpl extends GenericFacadeImpl implements UploadPropositionFacade {

	private final UploadPropositionService uploadPropositionService;

	public UploadPropositionFacadeImpl(final AccountService accountService,
			final UploadPropositionService uploadPropositionService) {
		super(accountService);
		this.uploadPropositionService = uploadPropositionService;
	}

	@Override
	public UploadProposition find(String actorUuid, String uuid) {
		Validate.notEmpty(uuid, "Upload proposition must be set");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		return uploadPropositionService.find(authUser, actor, uuid);
	}

	@Override
	public List<UploadProposition> findAllByAccountUuid(String actorUuid) throws BusinessException {
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		return uploadPropositionService.findAllByAccount(authUser, actor);
	}

	@Override
	public UploadProposition accept(String actorUuid, String uuid) {
		Validate.notEmpty(uuid, "upload Proposition uuid must be set");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		return uploadPropositionService.accept(authUser, actor, uuid);
	}

	@Override
	public UploadProposition reject(String actorUuid, String uuid) {
		Validate.notEmpty(uuid, "upload Proposition uuid must be set");
		User authUser = checkAuthentication();
		User actor = getActor(authUser, actorUuid);
		return uploadPropositionService.reject(authUser, actor, uuid);
	}
}
