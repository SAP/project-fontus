class Main  {

    public static void main(String[] args) {
        CentralDirectoryFileHeader cdfh = new CentralDirectoryFileHeader();
        AsciiBytes name = cdfh.getName();
        if(name != null) {
        System.out.println(name);
        } else {
            System.out.println("null");
        }
    }
}
