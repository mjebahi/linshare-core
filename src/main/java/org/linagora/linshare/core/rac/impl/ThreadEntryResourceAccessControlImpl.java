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

package org.linagora.linshare.core.rac.impl;

import org.linagora.linshare.core.domain.constants.TechnicalAccountPermissionType;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.ThreadEntry;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.rac.ThreadEntryResourceAccessControl;

public class ThreadEntryResourceAccessControlImpl extends
		EntryResourceAccessControlImpl<Account, ThreadEntry> implements
		ThreadEntryResourceAccessControl {

	public ThreadEntryResourceAccessControlImpl() {
		super();
	}

	@Override
	protected String getEntryRepresentation(ThreadEntry entry) {
		StringBuilder sb = new StringBuilder(entry.getEntryType().toString());
		sb.append(" (");
		sb.append(entry.getUuid());
		sb.append(") ");
		return sb.toString();
	}

	@Override
	protected Account getOwner(ThreadEntry entry, Object... opt) {
		Account owner = entry.getEntryOwner();
		return owner;
	}

	@Override
	protected String getOwnerRepresentation(Account owner) {
		return owner.getAccountReprentation();
	}

	@Override
	protected boolean hasReadPermission(Account actor, Account owner,
			ThreadEntry entry, Object... opt) {
		return defaultPermissionCheck(actor, owner, entry,
				TechnicalAccountPermissionType.THREAD_ENTRIES_GET);
	}

	@Override
	protected boolean hasListPermission(Account actor, Account owner,
			ThreadEntry entry, Object... opt) {
		return defaultPermissionCheck(actor, owner, entry,
				TechnicalAccountPermissionType.THREAD_ENTRIES_LIST);
	}

	@Override
	protected boolean hasDeletePermission(Account actor, Account owner,
			ThreadEntry entry, Object... opt) {
		return defaultPermissionCheck(actor, owner, entry,
				TechnicalAccountPermissionType.THREAD_ENTRIES_CREATE);
	}

	@Override
	protected boolean hasCreatePermission(Account actor, Account owner,
			ThreadEntry entry, Object... opt) {
		return defaultPermissionCheck(actor, owner, entry,
				TechnicalAccountPermissionType.THREAD_ENTRIES_CREATE);
	}

	@Override
	protected boolean hasUpdatePermission(Account actor, Account owner,
			ThreadEntry entry, Object... opt) {
		return defaultPermissionCheck(actor, owner, entry,
				TechnicalAccountPermissionType.THREAD_ENTRIES_UPDATE);
	}

	@Override
	protected boolean hasDownloadPermission(Account actor, Account owner,
			ThreadEntry entry, Object... opt) {
		return defaultPermissionCheck(actor, owner, entry,
				TechnicalAccountPermissionType.THREAD_ENTRIES_DOWNLOAD);
	}

	@Override
	protected boolean hasDownloadTumbnailPermission(Account actor,
			Account owner, ThreadEntry entry, Object... opt) {
		return defaultPermissionCheck(
				actor,
				owner,
				entry,
				TechnicalAccountPermissionType.THREAD_ENTRIES_DOWNLOAD_THUMBNAIL);
	}

	@Override
	public void checkDownloadPermission(Account actor, Account owner,
			Class<ThreadEntry> class1, BusinessErrorCode threadEntryForbidden) {
	}
}
