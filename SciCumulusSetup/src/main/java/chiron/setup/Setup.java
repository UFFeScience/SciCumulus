package chiron.setup;

import java.io.IOException;
import java.sql.SQLException;
import nu.xom.ParsingException;

/**
 *
 * @author jonasdias
 */
public class Setup {

    private boolean commit = false;
    private boolean delete = false;
    private boolean update = false;
    private String xmlFile;

    private void configure(String[] args) {
        if (args.length < 2) {
            System.err.println("You must specify the operation and the Workflow XML file.");
            this.showUsage();
            System.exit(1);
        } else {
            if (args[0].equals("--insert") || args[0].equals("-i")) {
                this.commit = true;
            } else if (args[0].equals("--update") || args[0].equals("-u")) {
                this.update = true;
            } else if (args[0].equals("--delete") || args[0].equals("-d")) {
                this.delete = true;
            } else {
                System.err.println("Wrong syntax.");
                this.showUsage();
            }
            this.xmlFile = args[1];
        }
    }

    public static void main(String[] args) {
        Setup setup = new Setup();
        setup.configure(args);
        Reader reader = new Reader();
        try {
            Workflow w = reader.readXML(setup.xmlFile);
            DBHandler handler = new DBHandler(reader.getDbAttribute("server"),
                    reader.getDbAttribute("port"),
                    reader.getDbAttribute("name"),
                    reader.getDbAttribute("username"),
                    reader.getDbAttribute("password"));
            if (setup.commit) {
                handler.commitWorkflow(w);
            } else if (setup.update) {
                handler.updateWorkflow(args[1]);
            } else if (setup.delete) {
                handler.deleteWorkflow(w, reader.getDbAttribute("server"),
                        reader.getDbAttribute("port"),
                        reader.getDbAttribute("name"),
                        reader.getDbAttribute("username"),
                        reader.getDbAttribute("password"));
            } else {
            }
        } catch (ParsingException ex) {
            System.err.println("Parsing Exception: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("I/O Exception: " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("SQL Exception: " + ex.getMessage());
        }
    }

    private void showUsage() {
        System.out.println("Usage: java -jar ChironSetup.jar [operation] [xml file]");
        System.out.println("Operations:\t --insert or -i\t\t Stores the workflow in the provenance database");
        System.out.println("\t\t --update or -u\t\t Update the already commited workflow in the provenance database");
        System.out.println("\t\t --delete or -d\t\t Deletes the workflow from the provenance database.");
        System.out.println("XML File:\tThe XML file with workflow conceptual description");
    }
}