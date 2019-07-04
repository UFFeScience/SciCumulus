package chiron.setup;

/**
 *
 * @author vitor
 */
class Operand {
    
    public String name;
    public String textValue;
    public float numericValue;

    Operand(String agreg_field, String textValue) {
        this.name = agreg_field;
        this.textValue = textValue;
    }
}
