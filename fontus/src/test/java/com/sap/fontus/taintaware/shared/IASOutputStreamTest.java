package com.sap.fontus.taintaware.shared;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASObjectInputStream;
import com.sap.fontus.taintaware.unified.IASString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IASOutputStreamTest {
    @BeforeAll
    public static void setup() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testTaintableStringRoundtrip() throws Exception {
        Widget w = Widget.makeWidget();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(w);
        oos.close();
        byte[] bytes = os.toByteArray();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        IASObjectInputStream iis = new IASObjectInputStream(is);
        Widget w2 = (Widget) iis.readObject();
        assertEquals(w, w2);
    }
    @Test
    void testTaintedStringRoundtrip() throws Exception {
        Widget w = Widget.makeWidget();
        w.setValue(IASString.tainted(w.getValue()));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(w);
        oos.close();
        byte[] bytes = os.toByteArray();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        IASObjectInputStream iis = new IASObjectInputStream(is);
        Widget w2 = (Widget) iis.readObject();
        assertEquals(w, w2);
    }
    static class Widget implements Serializable {
        private static final long serialVersionUID = 888L;

        private IASString value;
        private int number;

        static Widget makeWidget() {
            Widget w = new Widget();
            w.number = 1337;
            w.value = IASString.valueOf("HELLO THERE");
            return w;
        }


        public Widget() {
            this.value = null;
            this.number = -1;
        }

        public IASString getValue() {
            return this.value;
        }

        public void setValue(IASString value) {
            this.value = value;
        }

        public int getNumber() {
            return this.number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            IASOutputStreamTest.Widget widget = (IASOutputStreamTest.Widget) o;
            return this.number == widget.number && Objects.equals(this.value, widget.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value, this.number);
        }
    }
}
