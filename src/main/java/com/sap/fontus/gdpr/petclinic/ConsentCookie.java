package com.sap.fontus.gdpr.petclinic;


import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ConsentCookie {
    public List<ConsentCookie.Purpose> getPurposes() {
        return this.purposes;
    }

    public void setPurposes(List<ConsentCookie.Purpose> purposes) {
        this.purposes = purposes;
    }

    private List<ConsentCookie.Purpose> purposes;

    public ConsentCookie() {
        this.purposes = new ArrayList<>(1);
    }

    @Override
    public String toString() {
        return "ConsentCookie{" +
                "purposes=" + purposes +
                '}';
    }

    public static ConsentCookie parse(String encoded) {
        byte[] bs = Base64.getDecoder().decode(encoded);
        String value = new String(bs);
        Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).useFields(true, VisibilityFilter.PRIVATE).create();
        ConsentCookie cc = genson.deserialize(value, ConsentCookie.class);
        return cc;
    }

    static class Purpose {
        private String id;
        private List<ConsentCookie.Vendor> vendors;

        public Purpose() {
            this.vendors = new ArrayList<>(1);
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<ConsentCookie.Vendor> getVendors() {
            return this.vendors;
        }

        public void setVendors(List<ConsentCookie.Vendor> vendors) {
            this.vendors = vendors;
        }

        @Override
        public String toString() {
            return "Purpose{" +
                    "id='" + id + '\'' +
                    ", vendors=" + vendors +
                    '}';
        }
    }
    static class Vendor {
        private String id;
        private String name;
        private boolean checked;

        public Vendor() {
            this.id = null;
            this.name = null;
            this.checked = false;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isChecked() {
            return this.checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        @Override
        public String toString() {
            return "Vendor{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", checked=" + checked +
                    '}';
        }
    }
}
