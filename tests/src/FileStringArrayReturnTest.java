import java.io.IOException;
import java.io.File;

class FileStringArrayReturnTest {

    public static void main(String[] args) {
        File file = new File(args[0]);
        if(!file.isDirectory()) {
            System.err.println(String.format("'%s' is not a directory!", file));
            return;
        }
        String[] fileList = file.list();
        for(String fileName : fileList) {
            System.out.println(fileName);
        }
    }
}
