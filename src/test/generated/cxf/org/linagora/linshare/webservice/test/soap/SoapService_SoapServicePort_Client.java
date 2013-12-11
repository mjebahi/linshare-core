/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2013 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2013. Contribute to
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
package org.linagora.linshare.webservice.test.soap;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

/**
 * This class was generated by Apache CXF 2.7.0
 * 2013-01-17T17:17:27.931+01:00
 * Generated source version: 2.7.0
 * 
 */
public final class SoapService_SoapServicePort_Client {

    private static final QName SERVICE_NAME = new QName("http://org/linagora/linshare/webservice/", "SoapWebService");

    private SoapService_SoapServicePort_Client() {
    }

    public static void main(String args[]) throws java.lang.Exception {
        URL wsdlURL = SoapWebService.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) { 
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
      
        SoapWebService ss = new SoapWebService(wsdlURL, SERVICE_NAME);
        SoapService port = ss.getSoapServicePort();  
        
        {
        System.out.println("Invoking getInformation...");
        try {
            java.lang.String _getInformation__return = port.getInformation();
            System.out.println("getInformation.result=" + _getInformation__return);

        } catch (BusinessException_Exception e) { 
            System.out.println("Expected exception: BusinessException has occurred.");
            System.out.println(e.toString());
        }
            }
        {
        System.out.println("Invoking addDocumentXop...");
        org.linagora.linshare.webservice.test.soap.DocumentAttachement _addDocumentXop_arg0 = null;
        try {
            org.linagora.linshare.webservice.test.soap.Document _addDocumentXop__return = port.addDocumentXop(_addDocumentXop_arg0);
            System.out.println("addDocumentXop.result=" + _addDocumentXop__return);

        } catch (BusinessException_Exception e) { 
            System.out.println("Expected exception: BusinessException has occurred.");
            System.out.println(e.toString());
        }
            }
        {
        System.out.println("Invoking getAvailableSize...");
        try {
            org.linagora.linshare.webservice.test.soap.SimpleLongValue _getAvailableSize__return = port.getAvailableSize();
            System.out.println("getAvailableSize.result=" + _getAvailableSize__return);

        } catch (BusinessException_Exception e) { 
            System.out.println("Expected exception: BusinessException has occurred.");
            System.out.println(e.toString());
        }
            }
        {
        System.out.println("Invoking sharedocument...");
        java.lang.String _sharedocument_arg0 = "";
        java.lang.String _sharedocument_arg1 = "";
        int _sharedocument_arg2 = 0;
        try {
            port.sharedocument(_sharedocument_arg0, _sharedocument_arg1, _sharedocument_arg2);

        } catch (BusinessException_Exception e) { 
            System.out.println("Expected exception: BusinessException has occurred.");
            System.out.println(e.toString());
        }
            }
        {
        System.out.println("Invoking getDocuments...");
        try {
            java.util.List<org.linagora.linshare.webservice.test.soap.Document> _getDocuments__return = port.getDocuments();
            System.out.println("getDocuments.result=" + _getDocuments__return);

        } catch (BusinessException_Exception e) { 
            System.out.println("Expected exception: BusinessException has occurred.");
            System.out.println(e.toString());
        }
            }
        {
        System.out.println("Invoking getUserMaxFileSize...");
        try {
            org.linagora.linshare.webservice.test.soap.SimpleLongValue _getUserMaxFileSize__return = port.getUserMaxFileSize();
            System.out.println("getUserMaxFileSize.result=" + _getUserMaxFileSize__return);

        } catch (BusinessException_Exception e) { 
            System.out.println("Expected exception: BusinessException has occurred.");
            System.out.println(e.toString());
        }
            }

        System.exit(0);
    }

}
