package outerpackage;


import com.sap.fontus.taintaware.unified.IASString;

@Anno(value = "Hallo", array = {"Hallo", "Welt"})
public final class ApplicationClass {
    public IASString doStuff(IASString string) {
        return string.concat(new IASString("concat"));
    }
}
