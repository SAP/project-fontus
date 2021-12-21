class Main {

    public static void main(String[] args) {
        final String path = "deserialized.dat";
        if(args.length == 1 && args[0].equals("init")) {
            Serializer serializer = new Serializer();
            if(serializer.serialize(path)) {
                System.out.println("Serialization succeded");
                System.exit(0);
            } else {
                System.out.println("Serialization failed");
                System.exit(1);
            }
        } else if(args.length == 1) {
            Deserializer deserializer = new Deserializer();
            Structure structure = deserializer.deserialize(args[0]);
            System.out.println(structure);
        } else {
            System.err.println("No args given");
        }

    }
}
