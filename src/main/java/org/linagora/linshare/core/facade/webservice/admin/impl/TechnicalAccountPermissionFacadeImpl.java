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

package org.linagora.linshare.core.facade.webservice.admin.impl;

import org.apache.commons.lang3.Validate;
import org.linagora.linshare.core.domain.constants.Role;
import org.linagora.linshare.core.domain.entities.TechnicalAccountPermission;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.admin.TechnicalAccountPermissionFacade;
import org.linagora.linshare.core.facade.webservice.common.dto.TechnicalAccountPermissionDto;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.TechnicalAccountPermissionService;

public class TechnicalAccountPermissionFacadeImpl extends AdminGenericFacadeImpl
		implements TechnicalAccountPermissionFacade {

	private final TechnicalAccountPermissionService technicalAccountPermissionService;
	
	private final AbstractDomainService domainService;

	public TechnicalAccountPermissionFacadeImpl(final AccountService accountService,
			final TechnicalAccountPermissionService technicalAccountPermissionService,
			final AbstractDomainService domainService) {
		super(accountService);
		this.technicalAccountPermissionService = technicalAccountPermissionService;
		this.domainService = domainService;
	}

	@Override
	public TechnicalAccountPermissionDto find(String uuid) throws BusinessException {
		User authUser = checkAuth();
		Validate.notEmpty(uuid, "uuid must be set.");
		TechnicalAccountPermission permission = technicalAccountPermissionService.find(authUser, uuid);
		technicalAccountPermissionService.delete(authUser, permission);
		return new TechnicalAccountPermissionDto(permission);
	}

	@Override
	public TechnicalAccountPermissionDto update(TechnicalAccountPermissionDto dto)
			throws BusinessException {
		User authUser = checkAuth();
		Validate.notEmpty(dto.getUuid(), "uuid must be set.");
		TechnicalAccountPermission tap = new TechnicalAccountPermission(dto);
		for (String domain: dto.getDomains()) {
			if (domain != null)
				tap.addDomain(domainService.findById(domain));
		}
		TechnicalAccountPermission permission = technicalAccountPermissionService.update(authUser, tap);
		return new TechnicalAccountPermissionDto(permission);
	}

	/**
	 * Helpers
	 */
	
	private User checkAuth() throws BusinessException {
		return checkAuthentication(Role.SUPERADMIN);
	}
}
