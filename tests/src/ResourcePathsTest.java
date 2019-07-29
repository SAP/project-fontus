import java.util.Set;
import java.util.LinkedHashSet;
import java.io.IOException;
import java.io.File;

class ResourcePathsTest {

    private final String resourceBasePath;

    public ResourcePathsTest() {
        this.resourceBasePath = "";
    }

    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }
    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        //try {
            File file = new File(actualPath);
            String[] fileList = file.list();
            if (isEmpty(fileList)) {
                return null;
            }
            Set<String> resourcePaths = new LinkedHashSet<>(fileList.length);
            for (String fileEntry : fileList) {
                String resultPath = actualPath + fileEntry;
                resourcePaths.add(resultPath);
            }
            return resourcePaths;
        //}
        //catch (IOException ex) {
            //if (logger.isWarnEnabled()) {
            //	logger.warn("Could not get resource paths for " + resource, ex);
            //}
            return null;
        //}
    }

}
