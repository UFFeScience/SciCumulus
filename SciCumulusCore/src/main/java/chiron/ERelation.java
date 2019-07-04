package chiron;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author jonasdias, Vítor.
 */
public class ERelation implements Serializable {

    public String name;
    public String[] fields;
    public TreeMap<Integer, String[]> values;

    public ERelation(String relationName, String[] fields, TreeMap<Integer, String[]> values) {
        this.name = relationName;
        this.fields = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            this.fields[i] = fields[i].toUpperCase();
        }
        this.values = values;
    }

    public ERelation(String relationname, List<String> fields) {
        this.name = relationname;
        this.fields = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            this.fields[i] = fields.get(i).toUpperCase();
        }
        values = new TreeMap<Integer, String[]>();
    }

    public ERelation(String relationName, Integer key, HashMap<String, String> inputValues) {
        this.name = relationName;
        fields = new String[inputValues.keySet().size()];
        String[] relValues = new String[fields.length];
        int i = 0;
        for (String field : inputValues.keySet()) {
            relValues[i] = inputValues.get(field);
            fields[i] = field.toUpperCase();
            i++;
        }
        values = new TreeMap<Integer, String[]>();
        values.put(key, relValues);
    }

    public int size() {
        return values.keySet().size();
    }

    public Set<Integer> keySet() {
        return this.values.keySet();
    }

    public HashMap<String, String> getFirst() {
        String[] tupleValues = this.values.firstEntry().getValue();
        HashMap<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < fields.length; i++) {
            result.put(fields[i], tupleValues[i]);
        }
        return result;
    }

    public Integer getFirstKey() {
        if (!this.values.isEmpty()) {
            return this.values.firstKey();
        } else {
            return null;
        }
    }

    public Integer getLastKey() {
        if (!this.values.isEmpty()) {
            return this.values.lastKey();
        } else {
            return null;
        }
    }

    public String[] getTupleArray(Integer key) {
        return this.values.get(key);
    }

    public String getCSV(int inputKey, int workflowID) {
        String ik = String.valueOf(inputKey);
        String csv = "";
        for (Integer k : values.keySet()) {
            String[] tuple = values.get(k);
            csv += String.valueOf(workflowID) + ChironUtils.relationSeparator + ik;
            for (int i = 0; i < tuple.length; i++) {
                csv += ChironUtils.relationSeparator + tuple[i];
            }
            csv += ChironUtils.LINE_SEPARATOR;
        }
        return csv;
    }

    public String getCSV() {
        String csv = "";
        for (Integer k : values.keySet()) {
            String[] tuple = values.get(k);
            csv += tuple[0];
            for (int i = 1; i < tuple.length; i++) {
                csv += ChironUtils.relationSeparator + tuple[i];
            }
            csv += ChironUtils.LINE_SEPARATOR;
        }
        return csv;
    }

    public String getCSVHeader() {
        String retFields = fields[0].toUpperCase();
        for (int i = 1; i < fields.length; i++) {
            retFields += ChironUtils.relationSeparator + fields[i].toUpperCase();
        }
        return retFields + ChironUtils.LINE_SEPARATOR;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void putKey(Integer key) {
        this.values.put(key, null);
    }

    void resetKey(Integer firstKey, Integer newKey) {
        if (!firstKey.equals(newKey)) {
            values.put(newKey, values.get(firstKey));
            values.remove(firstKey);
        }
    }
}
