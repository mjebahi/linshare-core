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
package org.linagora.linshare.repository.hibernate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={
		"classpath:springContext-test.xml", 
		"classpath:springContext-datasource.xml",
		"classpath:springContext-repository.xml"})
public class LogEntryRepositoryImplTest {

	
	@Autowired
	private LogEntryRepository logEntryRepository;
	
//	private final Calendar actionDate = new GregorianCalendar();
//	private final String actorMail = "testActorMail";
//	private final String actorFirstname = "testActorFirstName";
//	private final String actorLastname = "testActorLastName";
//	private final String actorDomain = "testActorDomain";
//	private final String fileName = "testFileName";
//	private final Long fileSize = 10L;	
//	private final String fileType = "testExt";
//	private final String targetMail = "testTargetMail";
//	private final String targetFirstname = "testTargetFirstName";
//	private final String targetLastname= "testTargetLastName";
//	private final String targetDomain= "testTargetDomain";
	

	@Disabled //FIXME :  Handle issues and enable the test
	@Test
	public void testExistFileLogEntry() throws BusinessException{
//		LogEntry testFileLogEntry = new FileLogEntry(actionDate, actorMail, actorFirstname, actorLastname, 
//				actorDomain, LogAction.FILE_UPLOAD,
//				"test description", fileName, fileSize, fileType);
//		
//		logEntryRepository.create(testFileLogEntry);
//		
//		Assertions.assertTrue(logEntryRepository.findByUser(actorMail) != null);
//		Assertions.assertTrue(logEntryRepository.findByUser(actorMail).size() == 1);
//		
//		LogEntry tmpLogEntry = logEntryRepository.findByUser(actorMail).get(0);
//		
//		Assertions.assertTrue(tmpLogEntry instanceof FileLogEntry);
//		Assertions.assertTrue(tmpLogEntry.getActorFirstname().equals(actorFirstname));
//		Assertions.assertTrue(((FileLogEntry)tmpLogEntry).getLogAction().equals(LogAction.FILE_UPLOAD));
//
//		logEntryRepository.delete(tmpLogEntry);
	}
	
	
	@Disabled //FIXME :  Handle issues and enable the test
	@Test
	public void testExistUserLogEntry() throws BusinessException{
//		LogEntry testFileLogEntry = new UserLogEntry(actionDate, actorMail, actorFirstname, actorLastname, 
//				actorDomain, LogAction.USER_CREATE,
//				"test description", targetMail, targetFirstname, targetLastname, targetDomain, null);
//		
//		logEntryRepository.create(testFileLogEntry);
//		
//		Assertions.assertTrue(logEntryRepository.findByUser(actorMail) != null);
//		Assertions.assertTrue(logEntryRepository.findByUser(actorMail).size() == 1);
//		
//		LogEntry tmpLogEntry = logEntryRepository.findByUser(actorMail).get(0);
//		
//		Assertions.assertTrue(tmpLogEntry instanceof UserLogEntry);
//		Assertions.assertTrue(tmpLogEntry.getActorFirstname().equals(actorFirstname));
//		Assertions.assertTrue(((UserLogEntry)tmpLogEntry).getLogAction().equals(LogAction.USER_CREATE));
//		logEntryRepository.delete(tmpLogEntry);
	}
	
	@Disabled //FIXME :  Handle issues and enable the test
	@Test
	public void testExistShareLogEntry() throws BusinessException{
//		LogEntry testFileLogEntry = new ShareLogEntry(actionDate, actorMail, actorFirstname, actorLastname, 
//				actorDomain, LogAction.FILE_SHARE,
//				"test description", fileName, fileSize, fileType, targetMail, targetFirstname, targetLastname, targetDomain, null);
//		
//		logEntryRepository.create(testFileLogEntry);
//		
//		Assertions.assertTrue(logEntryRepository.findByUser(actorMail) != null);
//		Assertions.assertTrue(logEntryRepository.findByUser(actorMail).size() == 1);
//		
//		LogEntry tmpLogEntry = logEntryRepository.findByUser(actorMail).get(0);
//		
//		Assertions.assertTrue(tmpLogEntry instanceof ShareLogEntry);
//		Assertions.assertTrue(tmpLogEntry.getActorFirstname().equals(actorFirstname));
//		Assertions.assertTrue(((ShareLogEntry)tmpLogEntry).getLogAction().equals(LogAction.FILE_SHARE));
//		logEntryRepository.delete(tmpLogEntry);
	}
}
