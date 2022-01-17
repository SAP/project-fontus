public class Main {


    public static void main(String[] args) {
        try (ObjectPool<String> pool = new ObjectPool<>()) {
            pool.addObject("SUP");
            pool.addObject("SON");
            String obj = pool.getObject();
            while (obj != null) {
                System.out.println(obj);
                obj = pool.getObject();
            }
        }
    }

}
