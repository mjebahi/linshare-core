/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2017 LINAGORA
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
package org.linagora.linshare.business.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.cxf.helpers.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.linagora.linshare.core.business.service.DocumentEntryBusinessService;
import org.linagora.linshare.core.dao.FileDataStore;
import org.linagora.linshare.core.domain.constants.FileMetaDataKind;
import org.linagora.linshare.core.domain.constants.LinShareTestConstants;
import org.linagora.linshare.core.domain.entities.Document;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.entities.WorkGroup;
import org.linagora.linshare.core.domain.objects.FileMetaData;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.DocumentEntryRepository;
import org.linagora.linshare.core.repository.DocumentRepository;
import org.linagora.linshare.core.repository.UserRepository;
import org.linagora.linshare.core.service.ThreadService;
import org.linagora.linshare.mongo.entities.WorkGroupDocument;
import org.linagora.linshare.mongo.entities.WorkGroupFolder;
import org.linagora.linshare.mongo.entities.WorkGroupNode;
import org.linagora.linshare.mongo.entities.mto.AccountMto;
import org.linagora.linshare.mongo.repository.WorkGroupNodeMongoRepository;
import org.linagora.linshare.service.LoadingServiceTestDatas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.beust.jcommander.internal.Sets;

@ContextConfiguration(locations = {
		"classpath:springContext-datasource.xml",
		"classpath:springContext-repository.xml",
		"classpath:springContext-dao.xml",
		"classpath:springContext-ldap.xml",
		"classpath:springContext-business-service.xml",
		"classpath:springContext-service-miscellaneous.xml",
		"classpath:springContext-service.xml",
		"classpath:springContext-rac.xml",
		"classpath:springContext-fongo.xml",
		"classpath:springContext-storage-jcloud.xml",
		"classpath:springContext-test.xml",
		})
public class DocumentEntryBusinessServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static Logger logger = LoggerFactory.getLogger(DocumentEntryBusinessServiceImplTest.class);

	@Autowired
	private DocumentEntryBusinessService documentEntryBusinessService;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private DocumentEntryRepository documentEntryRepository;

	@Autowired
	private WorkGroupNodeMongoRepository workGroupNodeMongoRepository;

	@Autowired
	@Qualifier("userRepository")
	private UserRepository<User> userRepository;

	@Autowired
	private ThreadService threadService;

	WorkGroup workGroup;

	WorkGroupNode workGroupFolder;

	@Autowired
	private FileDataStore fileDataStore;

	private LoadingServiceTestDatas datas;

	private User jane;

	@Before
	public void setUp() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_SETUP);
		this.executeSqlScript("import-tests-default-domain-quotas.sql", false);
		this.executeSqlScript("import-tests-quota-other.sql", false);
		datas = new LoadingServiceTestDatas(userRepository);
		datas.loadUsers();
		jane = datas.getUser2();
		workGroup = threadService.create(jane, jane, "work_group_name_1");
		workGroupFolder = new WorkGroupFolder(new AccountMto(jane), "folder1", null, workGroup.getLsUuid());
		logger.debug(LinShareTestConstants.END_SETUP);
	}

	@After
	public void tearDown() throws Exception {
		logger.debug(LinShareTestConstants.BEGIN_TEARDOWN);
		logger.debug(LinShareTestConstants.END_TEARDOWN);
	}

	@Test
	public void testCreateDocument() throws BusinessException, IOException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		File tempFile = File.createTempFile("linshare-test-", ".tmp");
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("linshare-default.properties");
		IOUtils.copy(stream, new FileOutputStream(tempFile));
		Calendar cal = Calendar.getInstance();
		DocumentEntry createDocumentEntry = documentEntryBusinessService.createDocumentEntry(jane, tempFile,
				tempFile.length(), "file.txt", null, false, null, "text/plain", cal, false, null);
		Assert.assertTrue(documentEntryBusinessService.find(createDocumentEntry.getUuid()) != null);
//		Assert.assertTrue(documentEntryBusinessService.getDocumentThumbnailStream(createDocumentEntry,
//				ThumbnailType.SMALL) != null);
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void testCreateAndDeleteDocument() throws BusinessException, IOException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		File tempFile = File.createTempFile("linshare-test-", ".tmp");
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("linshare-default.properties");
		IOUtils.copy(stream, new FileOutputStream(tempFile));
		Calendar cal = Calendar.getInstance();
		DocumentEntry createDocumentEntry = documentEntryBusinessService.createDocumentEntry(jane, tempFile,
				tempFile.length(), "file.txt", null, false, null, "text/plain", cal, false, null);
		Assert.assertTrue(documentEntryBusinessService.find(createDocumentEntry.getUuid()) != null);
		Document document = createDocumentEntry.getDocument();
//		Map<ThumbnailType, Thumbnail> thumbnails = document.getThumbnails();
		documentEntryRepository.delete(createDocumentEntry);
		documentEntryBusinessService.deleteDocument(document);
		Assert.assertTrue(documentRepository.findByUuid(document.getUuid()) == null);
//		Assert.assertTrue(thumbnails != null);
		logger.debug(LinShareTestConstants.END_TEST);
	}

	@Test
	public void testUpdateDocument() throws BusinessException, IOException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		File tempFile = File.createTempFile("linshare-test-", ".tmp");
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("linshare-default.properties");
		IOUtils.copy(stream, new FileOutputStream(tempFile));
		Calendar cal = Calendar.getInstance();
		DocumentEntry createDocumentEntry = documentEntryBusinessService.createDocumentEntry(jane, tempFile,
				tempFile.length(), "file.txt", null, false, null, "text/plain", cal, false, null);
//		update the document
		createDocumentEntry = documentEntryBusinessService.updateDocumentEntry(jane, createDocumentEntry, tempFile,
				tempFile.length(), "file.png", false, null, "image/png", cal);
		Assert.assertTrue(documentEntryRepository.findById(createDocumentEntry.getUuid()).getType() == "image/png");
		logger.debug(LinShareTestConstants.END_TEST);
	}
	
	@Ignore
	@Test
	public void testUpdateThumbnail() throws BusinessException, IOException {
		logger.info(LinShareTestConstants.BEGIN_TEST);
		Document createdDocument = createDocument();
		Set<DocumentEntry> documentEntries = createdDocument.getDocumentEntries();
		List<WorkGroupDocument> wgDocuments = workGroupNodeMongoRepository
				.findByDocumentUuid(createdDocument.getUuid());
		Assert.assertTrue("The created document should have a thumbnail", createdDocument.getHasThumbnail());
		documentEntryBusinessService.updateThumbnail(createdDocument, jane);
		for (DocumentEntry docEntry : documentEntries) {
			Assert.assertEquals("The document entry and the document should have the same thumbnail status",
					createdDocument.getHasThumbnail(), docEntry.isHasThumbnail());
		}
		for (WorkGroupDocument wgDocument : wgDocuments) {
			Assert.assertEquals("The wgDocument entry and the document should have the same thumbnail status",
					createdDocument.getHasThumbnail(), wgDocument.getHasThumbnail());
		}
		logger.debug(LinShareTestConstants.END_TEST);
	}

	private Document createDocument() throws BusinessException, IOException {
		File tempFile = File.createTempFile("linshare-test-", ".tmp");
		tempFile.deleteOnExit();
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("linshare-default.properties");
		IOUtils.copy(stream, new FileOutputStream(tempFile));
		Calendar cal = Calendar.getInstance();
		DocumentEntry createDocumentEntry = documentEntryBusinessService.createDocumentEntry(jane, tempFile,
				tempFile.length(), "file", null, false, null, "text/plain", cal, false, null);
		Document document = createDocumentEntry.getDocument();
		Set<DocumentEntry> documentEntries = Sets.newHashSet();
		documentEntries.add(createDocumentEntry);
		document.setDocumentEntries(documentEntries);
		createWorkGroupDocument(jane, document);
		File tempThmbFile = File.createTempFile("thumbnail", "png");
		tempFile.deleteOnExit();
		FileMetaData metaDataThmb = new FileMetaData(FileMetaDataKind.THUMBNAIL_SMALL, "image/png",
				tempThmbFile.length(), "thumbnail");
		metaDataThmb = fileDataStore.add(tempThmbFile, metaDataThmb);
		document.setThmbUuid(metaDataThmb.getUuid());
		document.setComputeThumbnail(true);
		return document;
	}

	private WorkGroupNode createWorkGroupDocument(User authUser, Document document) {
		WorkGroupDocument node = new WorkGroupDocument(authUser, "testName", document, workGroup, workGroupFolder);
		node.setCreationDate(new Date());
		node.setModificationDate(new Date());
		node.setUploadDate(new Date());
		node.setCiphered(false);
		node.setLastAuthor(new AccountMto(authUser, true));
		return workGroupNodeMongoRepository.insert(node);
	}

}
