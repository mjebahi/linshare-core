package org.linagora.linshare.webservice.impl;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.linagora.linshare.core.domain.entities.Guest;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.WebServiceDocumentFacade;
import org.linagora.linshare.webservice.FineUploaderService;
import org.linagora.linshare.webservice.dto.DocumentDto;
import org.linagora.linshare.webservice.dto.FineUploaderDto;

public class FineUploaderServiceImpl extends WebserviceBase implements
		FineUploaderService {

	private static final String FILE = "qqfile";
	private static final String FILE_NAME = "filename";

	private final WebServiceDocumentFacade webServiceDocumentFacade;

	public FineUploaderServiceImpl(
			WebServiceDocumentFacade webServiceDocumentFacade) {
		super();
		this.webServiceDocumentFacade = webServiceDocumentFacade;
	}

	@Path("/receiver")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public FineUploaderDto upload(@Multipart(value = FILE) InputStream file,
			@Multipart(value = FILE_NAME, required = false) String fileName,
			MultipartBody body) {
		User actor = null;

		// Authentication, permission and error checking
		try {
			actor = webServiceDocumentFacade.checkAuthentication();
		} catch (BusinessException e) {
			throw analyseFaultREST(e);
		}
		if (actor instanceof Guest && !actor.getCanUpload()) {
			throw giveRestException(HttpStatus.SC_FORBIDDEN,
					"You are not authorized to use this service");
		}
		if (file == null) {
			throw giveRestException(HttpStatus.SC_BAD_REQUEST,
					"Missing file (check parameter file)");
		}
		// Ensure fileName and description aren't null
		if (fileName == null || fileName.isEmpty()) {
			fileName = body.getAttachment(FILE).getContentDisposition()
					.getParameter(FILE_NAME);
		}
		try {
			return new FineUploaderDto(true, "", webServiceDocumentFacade
					.uploadfile(file, fileName, "").getUuid());
		} catch (BusinessException e) {
			return new FineUploaderDto(false, e.getMessage(), null);
		}
	}

	@Path("/receiver/{uuid}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public FineUploaderDto delete(@PathParam("uuid") String uuid) {
		User actor = null;

		// Authentication, permission and error checking
		try {
			actor = webServiceDocumentFacade.checkAuthentication();
		} catch (BusinessException e) {
			throw analyseFaultREST(e);
		}
		if (actor instanceof Guest && !actor.getCanUpload()) {
			throw giveRestException(HttpStatus.SC_FORBIDDEN,
					"You are not authorized to use this service");
		}
		if (uuid == null || uuid.isEmpty()) {
			throw giveRestException(HttpStatus.SC_BAD_REQUEST,
					"Missing file (check parameter file)");
		}
		try {
			webServiceDocumentFacade.deleteFile(uuid);
			return new FineUploaderDto(true);
		} catch (BusinessException e) {
			return new FineUploaderDto(false, e.getMessage());
		}
	}
}
