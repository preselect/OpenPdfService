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

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Outline Items
 *
 * @author Moritz Munte <m.munte@preselect.com>
 */
@XmlRootElement
public class OutlineItems implements Serializable {
        @XmlElement(name = "outline")
        public List<OutlineItem> items;

        public OutlineItems() {
        }

        public OutlineItems(List<OutlineItem> items) {
                this.items = items;
        }

        public List<OutlineItem> getItems() {
                return items;
        }
        
        
}
