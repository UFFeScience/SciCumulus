package chiron.setup;

public class Field {

    private String fname;
    private String ftype;
    private int decimalplaces = -1;
    private String fileoperation = null;
    private String instrumented = null;
    private int relid;
    private int decimalplacesUp;

    public Field(String name) {
        this.fname = name;
    }

    public Field(int decimalplacesUp, String fname, String ftype, String fileoperation, String instrumented) {
        this.decimalplacesUp = decimalplacesUp;
        this.fname = fname;
        this.ftype = ftype;
        if (fileoperation != null) {
            this.fileoperation = fileoperation;
        } else {
            this.fileoperation = "";
        }
        if (instrumented != null) {
            this.instrumented = instrumented;
        } else {
            this.instrumented = "";
        }
    }

    public Field(int relid, int decimalplacesUp, String fname, String ftype, String fileoperation, String instrumented) {
        this.relid = relid;
        this.decimalplacesUp = decimalplacesUp;
        this.fname = fname;
        this.ftype = ftype;
        if (fileoperation != null) {
            this.fileoperation = fileoperation;
        } else {
            this.fileoperation = "";
        }
        if (instrumented != null) {
            this.instrumented = instrumented;
        } else {
            this.instrumented = "";
        }
        if(instrumented != null && instrumented.equalsIgnoreCase("t")){
            this.instrumented = "true";
        }
        if(instrumented != null && instrumented.equalsIgnoreCase("f")){
            this.instrumented = "false";
        }
    }

    public String getFname() {
        return fname.toLowerCase();
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getFtype() {
        return ftype;
    }

    public void setFtype(String ftype) {
        this.ftype = ftype;
    }

    public int getDecimalplaces() {
        return decimalplaces;
    }

    public void setDecimalplaces(int decimalplaces) {
        this.decimalplaces = decimalplaces;
    }

    public String getFileoperation() {
        return fileoperation;
    }

    public void setFileoperation(String fileoperation) {
        this.fileoperation = fileoperation;
    }

    public String getInstrumented() {
        return instrumented;
    }

    public String getInstrumentedSQL() {
        String s = null;
        if (this.instrumented.equalsIgnoreCase("")) {
            return s;
        } else {
            if(this.instrumented.equalsIgnoreCase("true")){
                return "T";
            }
            if(this.instrumented.equalsIgnoreCase("false")){
                return "F";
            }
            return this.instrumented;
        }
    }

    public void setInstrumented(String instrumented) {
        this.instrumented = instrumented;
    }

    public int getRelid() {
        return relid;
    }

    public void setRelid(int relid) {
        this.relid = relid;
    }

    public int getDecimalplacesUp() {
        return decimalplacesUp;
    }

    public void setDecimalplacesUp(int decimalplacesUp) {
        this.decimalplacesUp = decimalplacesUp;
    }

    public String getFtypetoSQL() {
        String t = null;
        if (this.ftype.equalsIgnoreCase("string")) {
            t = "CHARACTER VARYING(250)";
        } else if (this.ftype.equalsIgnoreCase("float")) {
            t = "DOUBLE PRECISION";
        } else if (this.ftype.equalsIgnoreCase("file")) {
            t = "CHARACTER VARYING(250)";
        } else {
            System.err.println("Unrecognized type for field " + this.getFname() + ": " + ftype);
            System.exit(1);
        }
        return t;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Field other = (Field) obj;
        if (this.relid != other.relid) {
            return false;
        }
        if (this.decimalplacesUp != other.decimalplacesUp) {
            return false;
        }
        if ((this.fname == null) ? (other.fname != null) : !this.fname.equalsIgnoreCase(other.fname)) {
            return false;
        }
        if ((this.fileoperation == null) ? (other.fileoperation != null) : !this.fileoperation.equalsIgnoreCase(other.fileoperation)) {
            return false;
        }
        if ((this.instrumented == null) ? (other.instrumented != null) : !this.instrumented.equalsIgnoreCase(other.instrumented)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.relid;
        hash = 17 * hash + this.decimalplacesUp;
        hash = 17 * hash + (this.fname != null ? this.fname.hashCode() : 0);
        hash = 17 * hash + (this.fileoperation != null ? this.fileoperation.hashCode() : 0);
        hash = 17 * hash + (this.instrumented != null ? this.instrumented.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "\n\t\t\tField{" + "fname=" + fname + ", ftype=" + ftype + ", fileoperation=" + fileoperation + ", instrumented=" + instrumented + ", decimalplacesUp=" + decimalplacesUp + '}';
    }

    public String getFileoperationSQL() {
        String s = null;
        if (this.fileoperation.equalsIgnoreCase("")) {
            return s;
        } else {
            return this.fileoperation.toUpperCase();
        }
    }
}