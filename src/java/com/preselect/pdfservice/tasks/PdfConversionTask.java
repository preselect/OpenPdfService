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
package com.preselect.pdfservice.tasks;

import java.io.IOException;
import java.util.List;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.preselect.pdfservice.resources.Callback;
import com.preselect.pdfservice.clients.HttpClient;
import com.preselect.pdfservice.resources.OutlineItem;
import com.preselect.pdfservice.resources.OutlineItems;
import com.preselect.pdfservice.resources.Status;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;


/**
 * Pdf Conversion Task class
 *
 * @author Moritz Munte <m.munte@preselect.com>
 */
public class PdfConversionTask extends Task {

        private String tocSource;

        @Override
        protected void exec() throws Exception {
                sendCallback(new Callback(Status.IN_PROGRESS, "PDF conversion started"));
                HttpClient httpClient = new HttpClient(tocSource);
                OutlineItems outline = (OutlineItems) httpClient.getJson(OutlineItems.class);
                splitIText(outline, getInputPath() + getFileName(), getOutputPath());
                sendCallback(new Callback(Status.IN_PROGRESS, "PDF conversion completed"));
        }

        public String getTocSource() {
                return tocSource;
        }

        public void setTocSource(String tocSource) {
                this.tocSource = tocSource;
        }

        public static void splitIText(OutlineItems outline, String inputFile, String outputPath) throws IOException, BadPdfFormatException, DocumentException {
                List<OutlineItem> outlineItems = filter(having(on(OutlineItem.class).getLevel(), equalTo(1)), outline.getItems());
                PdfReader reader = new PdfReader(inputFile);
                reader.consolidateNamedDestinations();
                int numberOfPages = reader.getNumberOfPages();

                // Check if there are outline items
                if (outlineItems.size() <= 0) {
                        throw new IllegalArgumentException("No outline items found!");
                }
                // Find start and end of each chapter
                for (int i = 0; i < outlineItems.size(); i++) {
                        OutlineItem outlineItem = outlineItems.get(i);
                        // fix for first chapter start at zero
                        int chapterStart = outlineItem.getPage() < 1 ? 1 : outlineItem.getPage();
                        int chapterEnd;
                        // if last chapter is not reached yet ...
                        if (outlineItems.size() > i + 1) {
                                // ... get first page of next chapter and ...
                                chapterEnd = outlineItems.get(i + 1).getPage();
                                // ... reduce it for one page, if it's not a one page chapter
                                if (chapterEnd != chapterStart) {
                                        chapterEnd--;
                                }
                        } else {
                                // Otherwise set the end of last chapter to the end of the document
                                chapterEnd = numberOfPages;
                        }
                        File file = new File(outputPath);
                        file.mkdirs();
                        String path = outputPath + File.separator + outlineItem.getId() + ".pdf";
                        copyDocument(reader, chapterStart, chapterEnd, path, outline);

                        Logger.getLogger(Task.class.getName()).log(Level.INFO, "Chapter: {0} - Start:{1} <-> End: {2}", new Object[]{outlineItem.getTitle(), chapterStart, chapterEnd});
                }
                reader.close();
        }

        private static void copyDocument(PdfReader reader, int start, int end, String path, OutlineItems outline) throws IOException, DocumentException {
                Document document = new Document();
                PdfSmartCopy copy = new PdfSmartCopy(document, new FileOutputStream(path));

                document.open();
                for (int i = (start - 1); i <= (end - 1);) {
                        copy.addPage(copy.getImportedPage(reader, ++i));
                }
                List<OutlineItem> outlineForChapter = getOutlineBetweenPages(outline, start, end);
                Iterator<OutlineItem> iterator = outlineForChapter.iterator();
                if (iterator.hasNext()) {
                        List<HashMap<String, Object>> bookmarksForChapter = getBookmarks(iterator.next(), iterator, 1);
                        SimpleBookmark.shiftPageNumbers(bookmarksForChapter, (-start + 1), null);
                        copy.setOutlines(bookmarksForChapter);
                }
                if (outlineForChapter.size() > 0) {
                        OutlineItem firstOutline = outlineForChapter.get(0);
                        document.addTitle(firstOutline.getTitle());
                }
                document.addCreator("Content Select");
                document.close();
                copy.close();
        }

        private static List<OutlineItem> getOutlineBetweenPages(OutlineItems outline, int start, int end) {
                start = (start == 1) ? 0 : start;     // Fix for pagenumber of cover chapter is 0
                List<OutlineItem> outlineItems = filter(
                        having(on(OutlineItem.class).getPage(), greaterThanOrEqualTo(start))
                        .and(having(on(OutlineItem.class).getPage(), lessThanOrEqualTo(end))), outline.getItems());

                return outlineItems;
        }

        private static List<HashMap<String, Object>> getBookmarks(OutlineItem currentOuline, Iterator<OutlineItem> iterator, int level) {
                List<HashMap<String, Object>> bookmarks = new ArrayList<HashMap<String, Object>>();
                do {
                        HashMap<String, Object> bookmark = new HashMap<String, Object>();
                        bookmarks.add(bookmark);
                        bookmark.put("Title", currentOuline.getTitle());
                        bookmark.put("Action", "GoTo");
                        bookmark.put("Page", String.valueOf(currentOuline.getPage()) + " Fit");
                        if (iterator.hasNext()) {
                                currentOuline = iterator.next();
                                if (currentOuline.getLevel() > level) {
                                        bookmark.put("Kids", getBookmarks(currentOuline, iterator, currentOuline.getLevel()));
                                }
                                if (currentOuline.getLevel() < level) {
                                        break;
                                }
                        }
                } while (iterator.hasNext());

                return bookmarks;
        }

        @Override
        protected void cleanup() throws IOException {
        }
}