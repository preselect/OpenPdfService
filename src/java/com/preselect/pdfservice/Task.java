/*
 * Copyright (C) 2013 Moritz Munte <m.munte@preselect.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.preselect.pdfservice;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;


/**
 * Abstract Task class
 *
 * @author Moritz Munte <m.munte@preselect.com>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConversionTask.class, name = "ConversionTask"),})
public abstract class Task implements Runnable, Serializable {

    private String id;
    private String documentId;
    private String inputPath;
    private String fileName;
    private String outputPath;
    private String callbackUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public void run() {
        try {
            exec();
        } catch (Exception ex) {
            String taskId = id != null ? id.toString() : "Unkown";
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, "Error during execution of " + taskId, ex);
            try {
                Callback callback = new Callback("ERROR", ex.toString());
                sendCallback(callback);
            } catch (Exception ex2) {
                Logger.getLogger(Task.class.getName()).log(Level.SEVERE, "Not able to send error callback", ex2);
            }
        } finally {
            try {
                cleanup();
            } catch (IOException ex) {
                Logger.getLogger(Task.class.getName()).log(Level.WARNING, "Cleanup failed.", ex);
            }
        }
    }

    public void sendData(Object json) {
        HttpClient httpClient = new HttpClient(outputPath);
        httpClient.send(json);
    }

    public void sendCallback(Callback callback) {
        HttpClient httpClient = new HttpClient(callbackUrl);
        httpClient.send(callback);
    }

    protected abstract void exec() throws IOException, BadPdfFormatException, DocumentException;

    protected abstract void cleanup() throws IOException;
}
