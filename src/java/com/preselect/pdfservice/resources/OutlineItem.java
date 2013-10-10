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
package com.preselect.pdfservice.resources;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Outline Item
 *
 * @author Moritz Munte <m.munte@preselect.com>
 */
@XmlRootElement
public class OutlineItem implements Serializable {
        @XmlElement
        public int id;
        @XmlElement
        public String title;
        @XmlElement
        public int level;
        @XmlElement
        public int page;

        public OutlineItem() {
        }

        public OutlineItem(String title, int level, int page) {
                this.title = title;
                this.level = level;
                this.page = page;
        }   

        public OutlineItem(int id, String title, int level, int page) {
                this.id = id;
                this.title = title;
                this.level = level;
                this.page = page;
        }

        public int getId() {
                return id;
        }

        public String getTitle() {
                return title;
        }
        
        public int getLevel() {
                return level;
        }

        public int getPage() {
                return page;
        }
        
        @Override
        public String toString() {
                return "{id: " + this.id + ", title: " + this.title + ", level: " + level + ", page: " + page + "}";   
        }
        
}
