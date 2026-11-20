package dev.joguenco.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author joguenco
 */
@XmlRootElement(name = "provider")
@XmlAccessorType(XmlAccessType.FIELD)
public class Provider {

    @XmlElement(name = "domain")
    private List<String> domain;

    public List<String> getDomain() {
        return domain;
    }

    public void setDomain(List<String> domain) {
        this.domain = domain;
    }
}
