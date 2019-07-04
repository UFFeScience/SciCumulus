package chiron;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a file to be consumed by an activation.
 *
 * @author Eduardo, Jonas, Vítor.
 * @since 2010-12-25
 */
public class EFile implements Serializable {

    public enum Operation {

        MOVE, MOVE_DELETE, COPY, COPY_DELETE
    };
    public Integer fileID = null;
    public boolean template = false;
    public boolean instrumented;
    private String fileDir = "";
    private String fileName = null;
    public Integer fileSize = null;
    public Date fileData = null;
    public String fieldName;
    public Operation fileOper;

    public EFile(boolean instrumented, String fieldName, Operation fileOper) {
        this.instrumented = instrumented;
        this.fieldName = fieldName;
        this.fileOper = fileOper;
    }

    /**
     * @param fileDir the fileDir to set
     */
    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        if (fileName.contains(ChironUtils.SEPARATOR)) {
            fileDir = "";
            String[] split = fileName.split(ChironUtils.SEPARATOR);
            this.fileName = split[split.length - 1];
            fileDir += "/";
            for (int i = 0; i < split.length - 1; i++) {
                split[i] = split[i].trim();
                if(split[i].equals("")) {
                    continue;
                }
                fileDir += split[i] + ChironUtils.SEPARATOR;
            }
            ChironUtils.checkDir(fileDir);
        } else {
            this.fileName = fileName;
        }
    }

    public String getFileDir() {
        return fileDir;
    }

    public String getFileName() {
        fileName = ChironUtils.getFileName(fileName);
        return fileName;
    }

    public String getPath() {
        return fileDir + fileName;
    }
}
