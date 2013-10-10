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

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;

/**
 * Img Conversion Task class
 *
 * @author Moritz Munte <m.munte@preselect.com>
 */
public class ImgConversionTask extends Task {

        @Override
        protected void exec() throws Exception {
                sendCallback(new Callback(Status.IN_PROGRESS, "IMG conversion started"));
                generateImages(getInputPath() + getFileName(), getOutputPath());
                sendCallback(new Callback(Status.IN_PROGRESS, "IMG conversion completed"));
        }

        void generateImages(String inputFile, String outputPath) throws Exception {
                // Load Document and metadata
                PDFDocument document = new PDFDocument();
                document.load(new File(inputFile));
                int pageCount = document.getPageCount() - 1;
                // load renderer
                SimpleRenderer renderer = new SimpleRenderer();
                renderer.setMaxProcessCount(2);
                renderer.setResolution(150);
                renderer.setAntialiasing(4);
                // create folders
                File file = new File(outputPath);
                file.mkdirs();
                // set JNA property
                // System.setProperty("jna.library.path", "C:\\Program Files\\gs\\gs9.09\\bin\\");
                // create image writer
                ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam iwp = imgWriter.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwp.setCompressionQuality(0.4f);
                // set parameter for iteration
                int offset = 15;
                int startPage = 0;
                int endPage = 0;
                // render images and save them
                do {
                        endPage = (endPage + offset < pageCount) ? endPage + offset : pageCount;
                        List<Image> images = renderer.render(document, startPage, endPage);
                        for (int i = 0; i < images.size(); i++) {
                                File imgFile = new File(outputPath + File.separatorChar + (startPage + i + 1) + ".jpg");
                                imgWriter.setOutput(new FileImageOutputStream(imgFile));
                                IIOImage image = new IIOImage((RenderedImage) images.get(i), null, null);
                                imgWriter.write(null, image, iwp);
                        }
                        sendCallback(new Callback(Status.IN_PROGRESS, (endPage + 1) + ":" + (pageCount + 1)));
                        startPage = endPage + 1;
                } while (endPage < pageCount);
        }

        @Override
        protected void cleanup() throws IOException {
        }
}
