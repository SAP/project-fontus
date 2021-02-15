package de.tubs.cs.ias.asm_test.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameter")
public class NetworkServletParameter {

        @XmlElement
        private final int index;

        public NetworkServletParameter() {
            this.index = -1;
        }

        public NetworkServletParameter(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public String toString() {
            return "SinkParameter{" +
                    "index=" + index +
                    '}';
        }
}
