package com.sap.fontus.gdpr.cookie;

class Vendor {
    private String id;
    private String name;
    private boolean checked;

    Vendor() {
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
        return String.format("Vendor{id='%s', name='%s', checked=%s}", this.id, this.name, this.checked);
    }
}
