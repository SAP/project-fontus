
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.ConfigurationLoader;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import org.apache.tomcat.util.net.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Example {

    public static void main(String[] args) {
        Configuration fontusConfig = ConfigurationLoader.loadAndMergeConfiguration(new File("./configuration.xml"), TaintMethod.RANGE);
        Configuration.setConfiguration(fontusConfig);

        SSLHostConfig config = new SSLHostConfig();
        List<String> ciphers = config.getJsseCipherNames();
        for ( String s : ciphers) {
            System.out.println(s);
            System.out.println(s.getClass().getName());
        }

        List<IASString> implemented = new ArrayList() {
            {
                add(new IASString("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384"));
                add(new IASString("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA"));
                add(new IASString("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256"));
                add(new IASString("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256"));
                add(new IASString("TLS_RSA_WITH_AES_256_CBC_SHA"));
            }
        };

        for ( IASString a : implemented ) {
            System.out.println(a);
            System.out.println(a.getClass().getName());
        }

        ciphers.retainAll(implemented);
        if (ciphers.isEmpty()) {
            throw new IllegalArgumentException("No ciphers supported!");
        }
    }

}
