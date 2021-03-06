package ru.concerteza.springtomcat.etomcat6;

import org.apache.catalina.LifecycleException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.jsslutils.extra.apachehttpclient.SslContextedSecureProtocolSocketFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.AbstractApplicationContext;
import ru.concerteza.springtomcat.etomcat6.context.EmbeddedXmlSpringContext;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static java.io.File.separator;

/**
 * User: alexey
 * Date: 8/29/11
 */
public abstract class TestSupertype {

    private AbstractApplicationContext ctx;

    @Before
    public void setUp() throws Exception {
        File baseDir = new File("src" + separator + "main" + separator + "app-dirs" + separator + dirname());
        File confDir = new File(baseDir, "conf");
        File ctxFile = new File(confDir, "etomcat-test-ctx.xml");
        ctx = new EmbeddedXmlSpringContext("file:" + ctxFile.getPath());
        ctx.getBean(EmbeddedTomcat.class).start(baseDir);
    }

    @After
    public void tearDown() throws LifecycleException {
        if(null != ctx) ctx.close();
    }

    protected void setupSsl() throws NoSuchProviderException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
        KeyStore trustStore = KeyStore.getInstance("JKS", "SUN");
        trustStore.load(TestSupertype.class.getResourceAsStream("/client-truststore.jks"), "amber%".toCharArray());
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
        fac.init(trustStore);
        TrustManager[] tms = fac.getTrustManagers();
        SSLContext ctx = SSLContext.getInstance("TLS", "SunJSSE");
        ctx.init(new KeyManager[] {}, tms, new SecureRandom());
        SslContextedSecureProtocolSocketFactory secureProtocolSocketFactory = new SslContextedSecureProtocolSocketFactory(ctx);
        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) secureProtocolSocketFactory, 8443));
    }

    protected abstract String dirname();
}
