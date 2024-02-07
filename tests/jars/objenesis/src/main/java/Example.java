import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class Example {

    private final InstantiatorStrategy strategy;

    public Example() {
        this.strategy =  new StdInstantiatorStrategy();
        System.out.printf("Chose %s as instantiator strategy%n", this.strategy.getClass().getName());
    }

    public Widget makeWidget() {
        ObjectInstantiator<Widget> instantiator = this.strategy.newInstantiatorOf(Widget.class);
        return (Widget) instantiator.newInstance();
    }

    public static void main(String[] args) {
        Example e = new Example();
        Widget w = e.makeWidget();
        System.out.printf("Made widget: '%s'%n", w.toString());
    }

}
