/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2014 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2014. Contribute to
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

import org.linagora.linshare.core.business.service.UploadRequestBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestEntryBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestGroupBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestHistoryBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestTemplateBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestUrlBusinessService;
import org.linagora.linshare.core.domain.entities.UploadRequest;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.UploadRequestRepository;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.UploadRequestService;

public class UploadRequestServiceImpl implements
		UploadRequestService {

	private final AbstractDomainService abstractDomainService;
	private final UploadRequestBusinessService uploadRequestBusinessService;
	private final UploadRequestEntryBusinessService uploadRequestEntryBusinessService;
	private final UploadRequestGroupBusinessService uploadRequestGroupBusinessService;
	private final UploadRequestHistoryBusinessService uploadRequestHistoryBusinessService;
	private final UploadRequestTemplateBusinessService uploadRequestTemplateBusinessService;
	private final UploadRequestUrlBusinessService uploadRequestUrlBusinessService;

	public UploadRequestServiceImpl(
			final AbstractDomainService abstractDomainService,
			final UploadRequestBusinessService uploadRequestBusinessService,
			final UploadRequestEntryBusinessService uploadRequestEntryBusinessService,
			final UploadRequestGroupBusinessService uploadRequestGroupBusinessService,
			final UploadRequestHistoryBusinessService uploadRequestHistoryBusinessService,
			final UploadRequestTemplateBusinessService uploadRequestTemplateBusinessService,
			final UploadRequestUrlBusinessService uploadRequestUrlBusinessService) {
		this.abstractDomainService = abstractDomainService;
		this.uploadRequestBusinessService = uploadRequestBusinessService;
		this.uploadRequestEntryBusinessService = uploadRequestEntryBusinessService;
		this.uploadRequestGroupBusinessService = uploadRequestGroupBusinessService;
		this.uploadRequestHistoryBusinessService = uploadRequestHistoryBusinessService;
		this.uploadRequestTemplateBusinessService = uploadRequestTemplateBusinessService;
		this.uploadRequestUrlBusinessService = uploadRequestUrlBusinessService;
	}

	@Override
	public UploadRequest findRequestByUuid(String uuid) {
		return uploadRequestBusinessService.findByUuid(uuid);
	}

	@Override
	public UploadRequest createRequest(UploadRequest req)
			throws BusinessException {
		return uploadRequestBusinessService.create(req);
	}

	@Override
	public UploadRequest updateRequest(UploadRequest req)
			throws BusinessException {
		return uploadRequestBusinessService.update(req);
	}

	@Override
	public void deleteRequest(UploadRequest req)
			throws BusinessException {
		uploadRequestBusinessService.delete(req);
	}
}
