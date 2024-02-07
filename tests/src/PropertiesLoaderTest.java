import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

class PropertiesLoaderTest {

    public static Map<String, String> loadProperties( InputStream inStream )
        throws IOException
    {
        try ( final InputStream stream = inStream )
        {
            Properties p = new Properties();
            p.load( stream );
            Map<String, String> map = new ConcurrentHashMap<>( p.size() );
            for ( String key : p.stringPropertyNames() )
            {
                map.put( key, p.getProperty( key ) );
            }
            return map;
        }
    }

    public static void main(String[] args) throws IOException {
        String ps = "db.url=localhost\ndb.password=sup\n";
        InputStream is = new ByteArrayInputStream(ps.getBytes());
        Map<String,String> properties = loadProperties(is);
        for(String key : properties.keySet()) {
            String value = properties.get(key);
            System.out.println(key + " = " + value);
        }
    }
}
