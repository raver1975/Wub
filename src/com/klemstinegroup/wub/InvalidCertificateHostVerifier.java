package com.klemstinegroup.wub;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class InvalidCertificateHostVerifier implements HostnameVerifier {
    public boolean verify(String paramString, SSLSession paramSSLSession) {
        return true;
    }
}
