package gov.nasa.gsfc.spdf.cdfj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * The Class CDFUrl.
 *
 * @author nand
 */
public class CDFUrl {

    /** The Constant DST_ROOT_CAX3_CERTIFICATE_ALIAS. */
    public static final String DST_ROOT_CAX3_CERTIFICATE_ALIAS = "DSTRootCAX3";

    /** The Constant X_509_CERTIFICATE_TYPE. */
    public static final String X_509_CERTIFICATE_TYPE = "X.509";

    /** The Constant SSL_CONTEXT_PROTOCOL_NAME. */
    public static final String SSL_CONTEXT_PROTOCOL_NAME = "TLSv1.2";

    static final Logger LOGGER = CDFLogging.newLogger(CDFUrl.class);

    static final String certificateAsString = "DST Root CA X3\n==============\n-----BEGIN CERTIFICATE-----\n"
            + "MIIDSjCCAjKgAwIBAgIQRK+wgNajJ7qJMDmGLvhAazANBgkqhkiG9w0BAQUFADA/MSQwIgYDVQQK\n"
            + "ExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMTDkRTVCBSb290IENBIFgzMB4X\n"
            + "DTAwMDkzMDIxMTIxOVoXDTIxMDkzMDE0MDExNVowPzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1\n"
            + "cmUgVHJ1c3QgQ28uMRcwFQYDVQQDEw5EU1QgUm9vdCBDQSBYMzCCASIwDQYJKoZIhvcNAQEBBQAD\n"
            + "ggEPADCCAQoCggEBAN+v6ZdQCINXtMxiZfaQguzH0yxrMMpb7NnDfcdAwRgUi+DoM3ZJKuM/IUmT\n"
            + "rE4Orz5Iy2Xu/NMhD2XSKtkyj4zl93ewEnu1lcCJo6m67XMuegwGMoOifooUMM0RoOEqOLl5CjH9\n"
            + "UL2AZd+3UWODyOKIYepLYYHsUmu5ouJLGiifSKOeDNoJjj4XLh7dIN9bxiqKqy69cK3FCxolkHRy\n"
            + "xXtqqzTWMIn/5WgTe1QLyNau7Fqckh49ZLOMxt+/yUFw7BZy1SbsOFU5Q9D8/RhcQPGX69Wam40d\n"
            + "utolucbY38EVAjqr2m7xPi71XAicPNaDaeQQmxkqtilX4+U9m5/wAl0CAwEAAaNCMEAwDwYDVR0T\n"
            + "AQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwHQYDVR0OBBYEFMSnsaR7LHH62+FLkHX/xBVghYkQ\n"
            + "MA0GCSqGSIb3DQEBBQUAA4IBAQCjGiybFwBcqR7uKGY3Or+Dxz9LwwmglSBd49lZRNI+DT69ikug\n"
            + "dB/OEIKcdBodfpga3csTS7MgROSR6cz8faXbauX+5v3gTt23ADq1cEmv8uXrAvHRAosZy5Q6XkjE\n"
            + "GB5YGV8eAlrwDPGxrancWYaLbumR9YbK+rlmM6pZW87ipxZzR8srzJmwN0jP41ZL9c8PDHIyh8bw\n"
            + "RLtTcm1D9SZImlJnt1ir/md2cXjbDaJWFBM5JDGFoqgCWjBH4d1QB7wCCZAA62RjYJsWvIjJEubS\n"
            + "fZGL+T0yjWW06XyxV3bqxbYoOb8VZRzI9neWagqNdwvYkQsEjgfbKbYK7p2CNTUQ\n-----END CERTIFICATE-----\n";

    static {

        String defaultTypeKeyStore = KeyStore.getDefaultType();

        String javaHome = System.getProperty("java.home");

        Path javaHomeCaCertsPath = Paths.get(javaHome, "lib", "security", "cacerts");

        String defaultTrustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

        try (InputStream newInputStream = Files.newInputStream(javaHomeCaCertsPath)) {

            KeyStore keyStore = KeyStore.getInstance(defaultTypeKeyStore);

            try {
                keyStore.load(newInputStream, "changeit".toCharArray());
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to load keyStore " + javaHomeCaCertsPath, e);
            }

            CertificateFactory certificateFactory = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE);

            ByteArrayInputStream certificateBais = new ByteArrayInputStream(
                    certificateAsString.getBytes(StandardCharsets.US_ASCII));

            Certificate certificate = certificateFactory.generateCertificate(certificateBais);

            try {
                keyStore.setCertificateEntry(DST_ROOT_CAX3_CERTIFICATE_ALIAS, certificate);
            } catch (KeyStoreException e) {
                throw new IllegalStateException(
                        "Failed to set certificate with alias " + DST_ROOT_CAX3_CERTIFICATE_ALIAS, e);
            }

            TrustManagerFactory trustManagerFactory;

            try {
                trustManagerFactory = TrustManagerFactory.getInstance(defaultTrustManagerAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(
                        "Failed to obtain TrustManagerFactory for algorithm" + defaultTrustManagerAlgorithm, e);
            }

            try {
                trustManagerFactory.init(keyStore);
            } catch (KeyStoreException e) {
                throw new IllegalStateException("Failed to initialize trustManagerFactory with algorithm "
                        + defaultTrustManagerAlgorithm + " for " + javaHomeCaCertsPath + " keyStore", e);
            }

            try {
                SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL_NAME);

                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                SSLContext.setDefault(sslContext);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(
                        "Failed to obtain an sslContext for " + SSL_CONTEXT_PROTOCOL_NAME + " protocol", e);
            } catch (KeyManagementException e) {
                throw new IllegalStateException("Failed to init sslContext", e);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read javaHomeCaCertsPath " + javaHomeCaCertsPath, e);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Failed to obtain keystore instance of type " + defaultTypeKeyStore, e);
        } catch (CertificateException e) {
            throw new IllegalStateException("Failed to generate certificate", e);
        }

    }

    final URL url;

    /**
     * Instantiates a new CDF url.
     *
     * @param u the u
     */
    public CDFUrl(final URL u) {
        this.url = u;
    }

    /**
     * Open connection.
     *
     * @return the URL connection
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public URLConnection openConnection() throws IOException {
        return this.url.openConnection();
    }
}
