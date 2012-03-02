//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.03.02 at 02:31:12 PM CET 
//


package jadex.benchmarking.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RepeatConfiguration">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="type" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *                       &lt;enumeration value="list"/>
 *                       &lt;enumeration value="space"/>
 *                       &lt;enumeration value="stochasticDistribution"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;attribute name="end" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="step" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="values" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="typeOfStochasticDistribution" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="lambda" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="alpha" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="sigma" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Actions">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{}Action" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="starttime" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="actiontype" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="create"/>
 *             &lt;enumeration value="read"/>
 *             &lt;enumeration value="update"/>
 *             &lt;enumeration value="delete"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "repeatConfiguration",
    "actions"
})
@XmlRootElement(name = "Sequence")
public class Sequence {

    @XmlElement(name = "RepeatConfiguration", required = true)
    protected Sequence.RepeatConfiguration repeatConfiguration;
    @XmlElement(name = "Actions", required = true)
    protected Sequence.Actions actions;
    @XmlAttribute(name = "starttime", required = true)
    protected long starttime;
    @XmlAttribute(name = "actiontype", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String actiontype;

    /**
     * Gets the value of the repeatConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link Sequence.RepeatConfiguration }
     *     
     */
    public Sequence.RepeatConfiguration getRepeatConfiguration() {
        return repeatConfiguration;
    }

    /**
     * Sets the value of the repeatConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Sequence.RepeatConfiguration }
     *     
     */
    public void setRepeatConfiguration(Sequence.RepeatConfiguration value) {
        this.repeatConfiguration = value;
    }

    /**
     * Gets the value of the actions property.
     * 
     * @return
     *     possible object is
     *     {@link Sequence.Actions }
     *     
     */
    public Sequence.Actions getActions() {
        return actions;
    }

    /**
     * Sets the value of the actions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Sequence.Actions }
     *     
     */
    public void setActions(Sequence.Actions value) {
        this.actions = value;
    }

    /**
     * Gets the value of the starttime property.
     * 
     */
    public long getStarttime() {
        return starttime;
    }

    /**
     * Sets the value of the starttime property.
     * 
     */
    public void setStarttime(long value) {
        this.starttime = value;
    }

    /**
     * Gets the value of the actiontype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActiontype() {
        return actiontype;
    }

    /**
     * Sets the value of the actiontype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActiontype(String value) {
        this.actiontype = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{}Action" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "action"
    })
    public static class Actions {

        @XmlElement(name = "Action", required = true)
        protected List<Action> action;

        /**
         * Gets the value of the action property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the action property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAction().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Action }
         * 
         * 
         */
        public List<Action> getAction() {
            if (action == null) {
                action = new ArrayList<Action>();
            }
            return this.action;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="type" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
     *             &lt;enumeration value="list"/>
     *             &lt;enumeration value="space"/>
     *             &lt;enumeration value="stochasticDistribution"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;attribute name="end" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="step" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="values" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="typeOfStochasticDistribution" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="lambda" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="alpha" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="sigma" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class RepeatConfiguration {

        @XmlAttribute(name = "type", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String type;
        @XmlAttribute(name = "end")
        protected String end;
        @XmlAttribute(name = "step")
        protected String step;
        @XmlAttribute(name = "values")
        protected String values;
        @XmlAttribute(name = "typeOfStochasticDistribution")
        protected String typeOfStochasticDistribution;
        @XmlAttribute(name = "lambda")
        protected String lambda;
        @XmlAttribute(name = "alpha")
        protected String alpha;
        @XmlAttribute(name = "sigma")
        protected String sigma;

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the end property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEnd() {
            return end;
        }

        /**
         * Sets the value of the end property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEnd(String value) {
            this.end = value;
        }

        /**
         * Gets the value of the step property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStep() {
            return step;
        }

        /**
         * Sets the value of the step property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStep(String value) {
            this.step = value;
        }

        /**
         * Gets the value of the values property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValues() {
            return values;
        }

        /**
         * Sets the value of the values property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValues(String value) {
            this.values = value;
        }

        /**
         * Gets the value of the typeOfStochasticDistribution property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTypeOfStochasticDistribution() {
            return typeOfStochasticDistribution;
        }

        /**
         * Sets the value of the typeOfStochasticDistribution property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTypeOfStochasticDistribution(String value) {
            this.typeOfStochasticDistribution = value;
        }

        /**
         * Gets the value of the lambda property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLambda() {
            return lambda;
        }

        /**
         * Sets the value of the lambda property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLambda(String value) {
            this.lambda = value;
        }

        /**
         * Gets the value of the alpha property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAlpha() {
            return alpha;
        }

        /**
         * Sets the value of the alpha property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha(String value) {
            this.alpha = value;
        }

        /**
         * Gets the value of the sigma property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSigma() {
            return sigma;
        }

        /**
         * Sets the value of the sigma property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSigma(String value) {
            this.sigma = value;
        }

    }

}
