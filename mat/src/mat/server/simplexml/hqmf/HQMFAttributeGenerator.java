package mat.server.simplexml.hqmf;

import javax.xml.xpath.XPathExpressionException;
import mat.model.clause.MeasureExport;
import mat.server.util.XmlProcessor;
import mat.shared.UUIDUtilClient;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
/**
 * @author jnarang
 *
 */
public class HQMFAttributeGenerator extends HQMFDataCriteriaElementGenerator{
	/**
	 * This method will look for attributes used in the subTree logic and then generate appropriate data criteria entries.
	 *
	 * @param qdmNode the qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void createDataCriteriaForAttributes(Node qdmNode, Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor, XmlProcessor simpleXmlprocessor, Node attributeQDMNode
			) throws XPathExpressionException {
		
		String attributeName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		String attributeMode = (String) attributeQDMNode.getUserData(ATTRIBUTE_MODE);
		/*if(NEGATION_RATIONALE.equals(attributeName)) {
			generateNegationRationalEntries(qdmNode, dataCriteriaElem,
					dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
		}else*/ if (START_DATETIME.equals(attributeName) || STOP_DATETIME.equals(attributeName)
				|| SIGNED_DATETIME.equals(attributeName)) {
			generateOrderTypeAttributes(qdmNode, dataCriteriaElem, dataCriteriaXMLProcessor,
					simpleXmlprocessor, attributeQDMNode);
		} else if(SIGNED_DATETIME.equals(attributeName)){
			Element timeNode = dataCriteriaXMLProcessor.getOriginalDoc().createElement(TIME);
			generateDateTimeAttributesTag(timeNode, attributeQDMNode, dataCriteriaElem, dataCriteriaXMLProcessor, true);
		}else if(ADMISSION_DATETIME.equalsIgnoreCase(attributeName)
				|| DISCHARGE_DATETIME.equalsIgnoreCase(attributeName)
				|| REMOVAL_DATETIME.equalsIgnoreCase(attributeName)
				|| ACTIVE_DATETIME.equalsIgnoreCase(attributeName)
				|| TIME.equalsIgnoreCase(attributeName)
				|| DATE.equalsIgnoreCase(attributeName)) {
			generateDateTimeAttributes(qdmNode, dataCriteriaElem,
					dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
		} else if (FACILITY_LOCATION_ARRIVAL_DATETIME.equalsIgnoreCase(attributeName)
				|| FACILITY_LOCATION_DEPARTURE_DATETIME.equalsIgnoreCase(attributeName)) {
			generateFacilityLocationTypeAttributes(qdmNode, dataCriteriaElem,
					dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
		} else if (DOSE.equalsIgnoreCase(attributeName)
				|| LENGTH_OF_STAY.equalsIgnoreCase(attributeName)) {
			generateDoseTypeAttributes(qdmNode, dataCriteriaElem,
					dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
		} else if (attributeName.equalsIgnoreCase(REFILLS)) {
			generateRepeatNumber(qdmNode, dataCriteriaXMLProcessor, dataCriteriaElem, attributeQDMNode);
		} else if (attributeName.equalsIgnoreCase(DISCHARGE_STATUS)) {
			generateDischargeStatus(qdmNode, dataCriteriaXMLProcessor, dataCriteriaElem, attributeQDMNode);
		} else if (attributeName.equalsIgnoreCase(INCISION_DATETIME)) {
			generateIncisionDateTimeTypeAttributes(qdmNode, dataCriteriaElem,
					dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
		}else if (VALUE_SET.equals(attributeMode)
				|| CHECK_IF_PRESENT.equals(attributeMode)
				|| attributeMode.startsWith(LESS_THAN)
				|| attributeMode.startsWith(GREATER_THAN)
				|| EQUAL_TO.equals(attributeMode)) {
			//handle "Value Set", "Check If Present" and comparison(less than, greater than, equals) mode
			generateOtherAttributes(qdmNode, dataCriteriaElem,
					dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
		}
	}
	
	/**
	 * Generate dose type attributes.
	 *
	 * @param qdmNode the qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void generateDoseTypeAttributes(Node qdmNode,
			Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor,
			XmlProcessor simpleXmlprocessor, Node attributeQDMNode) throws XPathExpressionException {
		
		String attrName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		String attrMode = (String) attributeQDMNode.getUserData(ATTRIBUTE_MODE);
		Node attrOID = attributeQDMNode.getAttributes().getNamedItem(OID);
		Node attrVersion = attributeQDMNode.getAttributes().getNamedItem("version");
		boolean isLengthOfStayValueSet = false;
		XmlProcessor templateXMLProcessor = TemplateXMLSingleton.getTemplateXmlProcessor();
		Node templateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/template[text()='"
				+ attrName.toLowerCase() + "']");
		Node targetNode = templateNode.getAttributes().getNamedItem("target");
		Element targetQuantityTag = null;
		if(targetNode!=null){
			targetQuantityTag = dataCriteriaElem.getOwnerDocument().createElement(targetNode.getNodeValue());
		}
		Node unitAttrib = attributeQDMNode.getAttributes().getNamedItem("unit");
		if(CHECK_IF_PRESENT.equals(attrMode)){
			targetQuantityTag.setAttribute(FLAVOR_ID, "ANY.NONNULL");
		}  else if(VALUE_SET.equals(attrMode)){
			if(attrName.equalsIgnoreCase(LENGTH_OF_STAY)){
				isLengthOfStayValueSet = true;
			} else {
				targetQuantityTag.setAttribute(NULL_FLAVOR, "UNK");
				Element translationNode = dataCriteriaElem.getOwnerDocument().createElement(TRANSLATION);
				translationNode.setAttribute("valueSet", attrOID.getNodeValue());
				String version = attrVersion.getNodeValue();
				boolean addVersionToValueTag = false;
				if("1.0".equals(version)) {
					if(qdmNode.getAttributes().getNamedItem("effectiveDate") !=null){
						version = qdmNode.getAttributes().getNamedItem("effectiveDate").getNodeValue();
						addVersionToValueTag = true;
					} else {
						addVersionToValueTag = false;
					}
				} else {
					addVersionToValueTag = true;
				}
				if(addVersionToValueTag) {
					translationNode.setAttribute("valueSetVersion", version);
				}
				Element displayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(DISPLAY_NAME);
				displayNameElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem(NAME).getNodeValue()
						+ " " + attributeQDMNode.getAttributes().getNamedItem(TAXONOMY).getNodeValue() + " Value Set");
				translationNode.appendChild(displayNameElem);
				targetQuantityTag.appendChild(translationNode);
			}
		} else if(attrMode.startsWith(Generator.LESS_THAN) || attrMode.startsWith(Generator.GREATER_THAN) || attrMode.equals(Generator.EQUAL_TO)){
			if(attrMode.equals(Generator.EQUAL_TO)){
				targetQuantityTag.setAttribute("value", attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				if(unitAttrib!=null){
					targetQuantityTag.setAttribute("unit", unitAttrib.getNodeValue());
				}
			} else if(attrMode.startsWith(Generator.LESS_THAN)){
				Element uncertainRangeNode=  dataCriteriaElem.getOwnerDocument().createElement("uncertainRange");
				if(attrMode.equals(Generator.LESS_THAN)){
					uncertainRangeNode.setAttribute("highClosed", "false");
				}
				Element lowNode = dataCriteriaElem.getOwnerDocument().createElement(LOW);
				if(attrName.equalsIgnoreCase(LENGTH_OF_STAY)){
					lowNode.setAttribute("xsi:type", "PQ");
				}
				lowNode.setAttribute("nullFlavor", "NINF");
				Element highNode = dataCriteriaElem.getOwnerDocument().createElement(HIGH);
				highNode.setAttribute("xsi:type", "PQ");
				highNode.setAttribute("value", attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				if(unitAttrib!=null){
					highNode.setAttribute("unit", unitAttrib.getNodeValue());
				}
				
				uncertainRangeNode.appendChild(lowNode);
				uncertainRangeNode.appendChild(highNode);
				targetQuantityTag.appendChild(uncertainRangeNode);
				
			} else if(attrMode.startsWith(Generator.GREATER_THAN)){
				Element uncertainRangeNode=  dataCriteriaElem.getOwnerDocument().createElement("uncertainRange");
				if(attrMode.equals(Generator.GREATER_THAN)){
					uncertainRangeNode.setAttribute("lowClosed", "false");
				}
				Element lowNode = dataCriteriaElem.getOwnerDocument().createElement(LOW);
				lowNode.setAttribute("xsi:type", "PQ");
				lowNode.setAttribute("value", attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				if(unitAttrib!=null){
					lowNode.setAttribute("unit", unitAttrib.getNodeValue());
				}
				Element highNode = dataCriteriaElem.getOwnerDocument().createElement(HIGH);
				if(attrName.equalsIgnoreCase(LENGTH_OF_STAY)){
					highNode.setAttribute("xsi:type", "PQ");
				}
				highNode.setAttribute("nullFlavor", "PINF");
				uncertainRangeNode.appendChild(lowNode);
				uncertainRangeNode.appendChild(highNode);
				targetQuantityTag.appendChild(uncertainRangeNode);
			}
		}
		String insertAfterNodeName = templateNode.getAttributes().getNamedItem("insertAfterNode").getNodeValue();
		if (insertAfterNodeName != null) {
			if(dataCriteriaElem.getElementsByTagName(insertAfterNodeName).item(0) != null) {
				Node outBoundElement =  dataCriteriaElem.getElementsByTagName(insertAfterNodeName).item(0).getNextSibling();
				if (outBoundElement != null) {
					outBoundElement.getParentNode().insertBefore(targetQuantityTag, outBoundElement);
				} else {
					if(!isLengthOfStayValueSet){
						dataCriteriaElem.appendChild(targetQuantityTag);
					}
				}
			} else {
				dataCriteriaElem.appendChild(targetQuantityTag);
			}
		} else {
			if(!isLengthOfStayValueSet){
				dataCriteriaElem.appendChild(targetQuantityTag);
			}
		}
		
	}
	
	/**
	 * Generate facility location type attributes.
	 *
	 * @param qdmNode the qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void generateFacilityLocationTypeAttributes(Node qdmNode, Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor, XmlProcessor simpleXmlprocessor, Node attributeQDMNode) throws XPathExpressionException {
		String attributeName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		XmlProcessor templateXMLProcessor = TemplateXMLSingleton.getTemplateXmlProcessor();
		Node templateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/template[text()='"
				+ attributeName.toLowerCase() + "']");
		if(templateNode == null){
			return;
		}
		if(templateNode.getAttributes().getNamedItem("includeSubTemplate") !=null){
			appendSubTemplateInFacilityAttribute(templateNode, dataCriteriaXMLProcessor, templateXMLProcessor, dataCriteriaElem, attributeQDMNode);
		}
		generateDateTimeAttributes(qdmNode, dataCriteriaElem,
				dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
	}
	
	/**
	 * Generate incision date time type attributes.
	 *
	 * @param qdmNode the qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void generateIncisionDateTimeTypeAttributes(Node qdmNode, Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor,
			XmlProcessor simpleXmlprocessor, Node attributeQDMNode) throws XPathExpressionException {
		String attributeName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		XmlProcessor templateXMLProcessor = TemplateXMLSingleton.getTemplateXmlProcessor();
		Node templateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/template[text()='"
				+ attributeName.toLowerCase() + "']");
		if(templateNode == null){
			return;
		}
		if(templateNode.getAttributes().getNamedItem("includeSubTemplate") !=null){
			appendSubTemplateNode(templateNode, dataCriteriaXMLProcessor, templateXMLProcessor, dataCriteriaElem, qdmNode,attributeQDMNode);
		}
		generateDateTimeAttributes(qdmNode, dataCriteriaElem,
				dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
	}
	
	/**
	 * Generate order type attributes.
	 *
	 * @param qdmNode the qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void generateOrderTypeAttributes(Node qdmNode, Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor,
			XmlProcessor simpleXmlprocessor, Node attributeQDMNode) throws XPathExpressionException {
		String qdmName = qdmNode.getAttributes().getNamedItem("datatype").getNodeValue();
		String attrName = (String)attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		XmlProcessor templateXMLProcessor = TemplateXMLSingleton.getTemplateXmlProcessor();
		Node templateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/template[text()='"
				+ qdmName.toLowerCase() + "']");
		if(templateNode == null){
			return;
		}
		if((templateNode.getAttributes().getNamedItem("includeOtherSubTemplate") !=null)
				|| attrName.equalsIgnoreCase(SIGNED_DATETIME)){
			appendSubTemplateWithOrderAttribute(templateNode, dataCriteriaXMLProcessor, templateXMLProcessor, dataCriteriaElem, qdmNode,attributeQDMNode);
		} else {
			generateDateTimeAttributes(qdmNode, dataCriteriaElem,
					dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
		}
	}
	
	/**
	 * Generate negation rational entries.
	 *
	 * @param qdmNode the qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void generateNegationRationalEntries(Node qdmNode, Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor,
			XmlProcessor simpleXmlprocessor, Node attributeQDMNode) throws XPathExpressionException {
		if(attributeQDMNode.getAttributes().getLength() > 0) {
			
			String attrName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
			String attribUUID = (String)attributeQDMNode.getUserData(ATTRIBUTE_UUID);
			
			XmlProcessor templateXMLProcessor = TemplateXMLSingleton.getTemplateXmlProcessor();
			Node templateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/template[text()='"
					+ attrName + "']");
			System.out.println("----------");
			System.out.println(attributeQDMNode.getNodeName());
			System.out.println(attributeQDMNode.getAttributes());
			System.out.println(simpleXmlprocessor.transform(attributeQDMNode));
			System.out.println("----------");
			String attributeValueSetName = attributeQDMNode.getAttributes()
					.getNamedItem(NAME).getNodeValue();
			String attributeOID = attributeQDMNode.getAttributes()
					.getNamedItem(OID).getNodeValue();
			String attributeTaxonomy = attributeQDMNode.getAttributes()
					.getNamedItem(TAXONOMY).getNodeValue();
			dataCriteriaElem.setAttribute("actionNegationInd", "true");
			
			Element outboundRelationshipElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(OUTBOUND_RELATIONSHIP);
			outboundRelationshipElem.setAttribute(TYPE_CODE, templateNode.getAttributes().getNamedItem(TYPE).getNodeValue());
			
			Element observationCriteriaElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(OBSERVATION_CRITERIA);
			observationCriteriaElem.setAttribute(CLASS_CODE, templateNode.getAttributes().getNamedItem(CLASS).getNodeValue());
			observationCriteriaElem.setAttribute(MOOD_CODE, templateNode.getAttributes().getNamedItem(MOOD).getNodeValue());
			
			outboundRelationshipElem.appendChild(observationCriteriaElem);
			
			Element templateId = dataCriteriaXMLProcessor
					.getOriginalDoc().createElement(TEMPLATE_ID);
			observationCriteriaElem.appendChild(templateId);
			
			Element itemChild = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(ITEM);
			itemChild.setAttribute(ROOT, templateNode.getAttributes().getNamedItem(OID).getNodeValue());
			itemChild.setAttribute("extension", VERSIONID);
			templateId.appendChild(itemChild);
			
			Element idElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(ID);
			idElem.setAttribute(ROOT, attribUUID);
			idElem.setAttribute("extension", StringUtils.deleteWhitespace(attributeValueSetName));
			observationCriteriaElem.appendChild(idElem);
			
			Element codeElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(CODE);
			codeElem.setAttribute(CODE, templateNode.getAttributes().getNamedItem(CODE).getNodeValue());
			codeElem.setAttribute(CODE_SYSTEM, templateNode.getAttributes().getNamedItem(CODE_SYSTEM).getNodeValue());
			
			Element displayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(DISPLAY_NAME);
			displayNameElem.setAttribute(VALUE, "Reason");
			
			observationCriteriaElem.appendChild(codeElem);
			codeElem.appendChild(displayNameElem);
			
			Element titleElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(TITLE);
			titleElem.setAttribute(VALUE, "Reason");
			observationCriteriaElem.appendChild(titleElem);
			
			Element valueElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(VALUE);
			valueElem.setAttribute(XSI_TYPE, templateNode.getAttributes().getNamedItem("valueType").getNodeValue());
			valueElem.setAttribute("valueSet", attributeOID);
			/*addValueSetVersion(attributeQDMNode, valueElem);*/
			Element valueDisplayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(DISPLAY_NAME);
			valueDisplayNameElem.setAttribute(VALUE, attributeValueSetName+" "+attributeTaxonomy+" Value Set");
			
			valueElem.appendChild(valueDisplayNameElem);
			observationCriteriaElem.appendChild(valueElem);
			
			dataCriteriaElem.appendChild(outboundRelationshipElem);
		}
	}
	
	/**
	 * Generate other attribute entries.
	 *
	 * @param qdmNode the qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void generateOtherAttributes(Node qdmNode, Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor,
			XmlProcessor simpleXmlprocessor, Node attributeQDMNode) throws XPathExpressionException {
		
		String attrName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		String attrMode = (String) attributeQDMNode.getUserData(ATTRIBUTE_MODE);
		String attribUUID = (String)attributeQDMNode.getUserData(ATTRIBUTE_UUID);
		String qdmName = qdmNode.getAttributes().getNamedItem("datatype").getNodeValue();
		boolean isResult = "result".equalsIgnoreCase(attrName);
		//boolean isStatus = "status".equalsIgnoreCase(attrName);
		boolean isResultNotOutBound = isResult && ("Diagnostic Study, Performed".equalsIgnoreCase(qdmName) || "Laboratory Test, Performed".equalsIgnoreCase(qdmName)
				|| "Functional Status, Performed".equalsIgnoreCase(qdmName) || "Risk Category Assessment".equalsIgnoreCase(qdmName));
		//boolean isResultValueset = (isResult && attrMode.equalsIgnoreCase(VALUE_SET));
		XmlProcessor templateXMLProcessor = TemplateXMLSingleton.getTemplateXmlProcessor();
		Node templateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/template[text()='"
				+ attrName.toLowerCase() + "']");
		boolean isRadiation = false;
		if(templateNode == null){
			templateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/template[text()='"
					+ attrName.toLowerCase()+"-" +attrMode.toLowerCase() + "']");
			if(templateNode == null) {
				return;
			} else {
				if (ANATOMICAL_LOCATION_SITE.equalsIgnoreCase(attrName)
						|| ORDINALITY.equalsIgnoreCase(attrName)
						|| ROUTE.equalsIgnoreCase(attrName)
						|| "method".equalsIgnoreCase(attrName)
						|| ANATOMICAL_APPROACH_SITE.equalsIgnoreCase(attrName)) {
					addTargetSiteOrPriorityCodeOrRouteCodeElement(dataCriteriaElem, dataCriteriaXMLProcessor, attributeQDMNode, templateNode);
				} else if(LATERALITY.equalsIgnoreCase(attrName)){
					appendSubTemplateNode(templateNode, dataCriteriaXMLProcessor, templateXMLProcessor, dataCriteriaElem,qdmNode,attributeQDMNode);
				} /*else if(ORDINALITY.equalsIgnoreCase(attrName)){
					addTargetSiteOrPriorityCodeOrRouteCodeElement(dataCriteriaElem, dataCriteriaXMLProcessor, attributeQDMNode, templateNode);
				}else if (ROUTE.equalsIgnoreCase(attrName)){
					addTargetSiteOrPriorityCodeOrRouteCodeElement(dataCriteriaElem, dataCriteriaXMLProcessor, attributeQDMNode, templateNode);
				} else if("method".equalsIgnoreCase(attrName)){
					addTargetSiteOrPriorityCodeOrRouteCodeElement(dataCriteriaElem, dataCriteriaXMLProcessor, attributeQDMNode, templateNode);
				}*/
				return;
			}
		}//flag to add statusCode for Radiation Dosage and Radiation Duration attributes
		if(templateNode.getAttributes().getNamedItem("isRadiation")!=null){
			isRadiation = templateNode.getAttributes().getNamedItem("isRadiation").getNodeValue()!=null;
		}
		if (attrName.equalsIgnoreCase(FACILITY_LOCATION)) {
			if (templateNode.getAttributes().getNamedItem("includeSubTemplate") !=null) {
				appendSubTemplateInFacilityAttribute(templateNode, dataCriteriaXMLProcessor, templateXMLProcessor, dataCriteriaElem, attributeQDMNode);
			}
			return;
		}
		Element outboundRelationshipElem = null;
		Element observationCriteriaElem = null;
		if(!isResultNotOutBound){ //result attribute with specific Datatypes does'nt add OutBoundRelationShip
			outboundRelationshipElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(OUTBOUND_RELATIONSHIP);
			outboundRelationshipElem.setAttribute(TYPE_CODE, templateNode.getAttributes().getNamedItem(TYPE).getNodeValue());
			
			Node invAttribNode = templateNode.getAttributes().getNamedItem("inv");
			if(invAttribNode != null){
				outboundRelationshipElem.setAttribute("inversionInd", invAttribNode.getNodeValue());
			}
			
			observationCriteriaElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(OBSERVATION_CRITERIA);
			observationCriteriaElem.setAttribute(CLASS_CODE, templateNode.getAttributes().getNamedItem(CLASS).getNodeValue());
			observationCriteriaElem.setAttribute(MOOD_CODE, templateNode.getAttributes().getNamedItem(MOOD).getNodeValue());
			
			outboundRelationshipElem.appendChild(observationCriteriaElem);
			
			if(templateNode.getAttributes().getNamedItem(OID) != null){
				Element templateId = dataCriteriaXMLProcessor
						.getOriginalDoc().createElement(TEMPLATE_ID);
				observationCriteriaElem.appendChild(templateId);
				
				Element itemChild = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(ITEM);
				itemChild.setAttribute(ROOT, templateNode.getAttributes().getNamedItem(OID).getNodeValue());
				itemChild.setAttribute("extension", VERSIONID);
				templateId.appendChild(itemChild);
			}
			
			Element idElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(ID);
			idElem.setAttribute(ROOT, attribUUID);
			idElem.setAttribute("extension", StringUtils.deleteWhitespace(attrName));
			observationCriteriaElem.appendChild(idElem);
			Element codeElem = createCodeForDatatype(templateNode, dataCriteriaXMLProcessor);
			if((isRadiation || isResult) && (codeElem!=null)){
				observationCriteriaElem.appendChild(codeElem);
			} else {
				Element displayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(DISPLAY_NAME);
				if(templateNode.getAttributes().getNamedItem("displayNameValue") != null){
					displayNameElem.setAttribute(VALUE, templateNode.getAttributes().getNamedItem("displayNameValue").getNodeValue());
				}else{
					displayNameElem.setAttribute(VALUE, attrName);
				}
				if (codeElem != null) {
					observationCriteriaElem.appendChild(codeElem);
					codeElem.appendChild(displayNameElem);
				}
			}
			if(!isRadiation){
				Element titleElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(TITLE);
				titleElem.setAttribute(VALUE, attrName);
				observationCriteriaElem.appendChild(titleElem);
			}  if(isRadiation){//statusCode is added for Radiation Duration and Dosage
				Element statusCodeElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(STATUS_CODE);
				if(templateNode.getAttributes().getNamedItem("status") != null){
					statusCodeElem.setAttribute(CODE, templateNode.getAttributes().getNamedItem("status").getNodeValue());
				}
				observationCriteriaElem.appendChild(statusCodeElem);
			}
		}
		Element valueElem =  dataCriteriaXMLProcessor.getOriginalDoc()
				.createElement(VALUE);
		if(VALUE_SET.equals(attrMode)){
			checkIfSelectedModeIsValueSet(dataCriteriaXMLProcessor, attributeQDMNode, templateNode,valueElem);
		} else if(CHECK_IF_PRESENT.equalsIgnoreCase(attrMode)){
			checkIfSelectedModeIsPresent(dataCriteriaXMLProcessor, attributeQDMNode, templateNode, valueElem);
		}else if(EQUAL_TO.equals(attrMode) || attrMode.startsWith(LESS_THAN) || attrMode.startsWith(GREATER_THAN)){
			checkIfSelectedModeIsArthimaticExpr(dataCriteriaXMLProcessor, attributeQDMNode, templateNode,valueElem);
		}
		if((outboundRelationshipElem!=null) && (observationCriteriaElem!=null)){
			observationCriteriaElem.appendChild(valueElem);
			dataCriteriaElem.appendChild(outboundRelationshipElem);
		} else {
			dataCriteriaElem.appendChild(valueElem);
		}
	}
	
	/**
	 * Refills Attribute tags.
	 *
	 * @param templateNode the template node
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param dataCriteriaElem the data criteria elem
	 * @param attributeQDMNode the attribute qdm node
	 */
	private void generateRepeatNumber(Node templateNode, XmlProcessor dataCriteriaXMLProcessor,
			Element dataCriteriaElem, Node attributeQDMNode) {
		String attrMode = (String) attributeQDMNode.getUserData(ATTRIBUTE_MODE);
		Element repeatNumberElement =  dataCriteriaXMLProcessor.getOriginalDoc()
				.createElement("repeatNumber");
		if (CHECK_IF_PRESENT.equalsIgnoreCase(attrMode)) {
			repeatNumberElement.setAttribute(FLAVOR_ID, "ANY.NONNULL");
			dataCriteriaElem.appendChild(repeatNumberElement);
		}  else if (EQUAL_TO.equals(attrMode) || attrMode.startsWith(LESS_THAN) || attrMode.startsWith(GREATER_THAN)) {
			if (EQUAL_TO.equals(attrMode)) {
				Element lowElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(LOW);
				lowElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				
				Element highElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(HIGH);
				highElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				repeatNumberElement.appendChild(lowElem);
				repeatNumberElement.appendChild(highElem);
				dataCriteriaElem.appendChild(repeatNumberElement);
			} else if(attrMode.startsWith(GREATER_THAN)) {
				if (attrMode.equals(GREATER_THAN)) {
					repeatNumberElement.setAttribute("lowClosed", "false");
				}
				Element lowElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(LOW);
				lowElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				repeatNumberElement.appendChild(lowElem);
				dataCriteriaElem.appendChild(repeatNumberElement);
			}else if(attrMode.startsWith(LESS_THAN)){
				if(attrMode.equals(LESS_THAN)){
					repeatNumberElement.setAttribute("highClosed", "false");
				}
				Element highElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(HIGH);
				highElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				repeatNumberElement.appendChild(highElem);
				dataCriteriaElem.appendChild(repeatNumberElement);
			}
		}
	}
	
	/**
	 * Discharge Status Attribute tags.
	 *
	 * @param templateNode the template node
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param dataCriteriaElem the data criteria elem
	 * @param attributeQDMNode the attribute qdm node
	 */
	private void generateDischargeStatus(Node templateNode, XmlProcessor dataCriteriaXMLProcessor,
			Element dataCriteriaElem, Node attributeQDMNode) {
		String attrMode = (String) attributeQDMNode.getUserData(ATTRIBUTE_MODE);
		Element dischargeDispositionElement =  dataCriteriaXMLProcessor.getOriginalDoc()
				.createElement("dischargeDispositionCode");
		if (CHECK_IF_PRESENT.equalsIgnoreCase(attrMode)) {
			dischargeDispositionElement.setAttribute(FLAVOR_ID, "ANY.NONNULL");
			dataCriteriaElem.appendChild(dischargeDispositionElement);
		}  else if (VALUE_SET.equalsIgnoreCase(attrMode)) {
			String attributeValueSetName = attributeQDMNode.getAttributes()
					.getNamedItem(NAME).getNodeValue();
			
			String attributeOID = attributeQDMNode.getAttributes()
					.getNamedItem(OID).getNodeValue();
			String attributeTaxonomy = attributeQDMNode.getAttributes()
					.getNamedItem(TAXONOMY).getNodeValue();
			dischargeDispositionElement.setAttribute("valueSet", attributeOID);
			addValueSetVersion(attributeQDMNode, dischargeDispositionElement);
			Element valueDisplayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(DISPLAY_NAME);
			valueDisplayNameElem.setAttribute(VALUE, attributeValueSetName+" "+attributeTaxonomy+" Value Set");
			dischargeDispositionElement.appendChild(valueDisplayNameElem);
			dataCriteriaElem.appendChild(dischargeDispositionElement);
		}
	}
	
	/**
	 * Check if selected mode is value set.
	 *
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param attributeQDMNode the attribute qdm node
	 * @param templateNode the template node
	 * @param valueElem the value elem
	 * @return the element
	 */
	private Element checkIfSelectedModeIsValueSet(XmlProcessor dataCriteriaXMLProcessor, Node attributeQDMNode,
			Node templateNode,Element valueElem) {
		String attributeValueSetName = attributeQDMNode.getAttributes()
				.getNamedItem(NAME).getNodeValue();
		String attributeOID = attributeQDMNode.getAttributes()
				.getNamedItem(OID).getNodeValue();
		String attributeTaxonomy = attributeQDMNode.getAttributes()
				.getNamedItem(TAXONOMY).getNodeValue();
		if(templateNode.getAttributes().getNamedItem("valueType") !=null) {
			valueElem.setAttribute(XSI_TYPE, templateNode.getAttributes().getNamedItem("valueType").getNodeValue());
		}
		
		valueElem.setAttribute("valueSet", attributeOID);
		addValueSetVersion(attributeQDMNode, valueElem);
		Element valueDisplayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
				.createElement(DISPLAY_NAME);
		valueDisplayNameElem.setAttribute(VALUE, attributeValueSetName+" "+attributeTaxonomy+" Value Set");
		valueElem.appendChild(valueDisplayNameElem);
		
		return valueElem;
	}
	
	
	/**
	 * Check if selected mode is present.
	 *
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param attributeQDMNode the attribute qdm node
	 * @param templateNode the template node
	 * @param valueElem the value elem
	 * @return the element
	 */
	private Element checkIfSelectedModeIsPresent(XmlProcessor dataCriteriaXMLProcessor, Node attributeQDMNode,
			Node templateNode,Element valueElem){
		valueElem.setAttribute(XSI_TYPE, "ANY");
		valueElem.setAttribute(FLAVOR_ID, "ANY.NONNULL");
		return valueElem;
	}
	
	/**
	 * Check if selected mode is arthimatic expr.
	 *
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param attributeQDMNode the attribute qdm node
	 * @param templateNode the template node
	 * @param valueElem the value elem
	 * @return the element
	 */
	private Element checkIfSelectedModeIsArthimaticExpr(XmlProcessor dataCriteriaXMLProcessor, Node attributeQDMNode,
			Node templateNode, Element valueElem){
		String attrMode = (String) attributeQDMNode.getUserData(ATTRIBUTE_MODE);
		String attrName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		String nodeName = attributeQDMNode.getNodeName();
		boolean isRadiation = false;
		boolean isResult = "result".equalsIgnoreCase(attrName);
		if(templateNode.getAttributes().getNamedItem("isRadiation")!=null){
			isRadiation = templateNode.getAttributes().getNamedItem("isRadiation").getNodeValue()!=null;
		}
		
		if(nodeName.equals("attribute"))
		{
			valueElem.setAttribute(XSI_TYPE, "IVL_PQ");
			Node unitAttrib = attributeQDMNode.getAttributes().getNamedItem("unit");
			if(EQUAL_TO.equals(attrMode) ){
				if(isRadiation){ //for radiation dosage and radiation duration
					valueElem.getAttributes().getNamedItem(XSI_TYPE).setNodeValue("PQ");
					valueElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
					if (unitAttrib != null) {
						String unitString = getUnitString(unitAttrib.getNodeValue());
						valueElem.setAttribute("unit", unitString);
					}
				} else { //for attributes other than radiation duration and radiation dosage
					Element lowElem = dataCriteriaXMLProcessor.getOriginalDoc()
							.createElement(LOW);
					lowElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
					
					Element highElem = dataCriteriaXMLProcessor.getOriginalDoc()
							.createElement(HIGH);
					highElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
					
					if(unitAttrib != null){
						String unitString = getUnitString(unitAttrib.getNodeValue());
						lowElem.setAttribute("unit", unitString);
						highElem.setAttribute("unit", unitString);
					}
					if(isResult){
						lowElem.setAttribute(XSI_TYPE, "PQ");
						highElem.setAttribute(XSI_TYPE, "PQ");
					}
					valueElem.appendChild(lowElem);
					valueElem.appendChild(highElem);
				}
				
			}else if(attrMode.startsWith(GREATER_THAN)){
				if(attrMode.equals(GREATER_THAN)){
					valueElem.setAttribute("lowClosed", "false");
				}
				Element lowElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(LOW);
				if(isResult){
					lowElem.setAttribute(XSI_TYPE, "PQ");
				}
				lowElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				if(unitAttrib != null){
					String unitString = getUnitString(unitAttrib.getNodeValue());
					lowElem.setAttribute("unit", unitString);
				}
				valueElem.appendChild(lowElem);
				if(isRadiation || isResult){//for radiation dosage and radiation duration
					Element highElem = dataCriteriaXMLProcessor.getOriginalDoc()
							.createElement(HIGH);
					highElem.setAttribute(NULL_FLAVOR, "PINF");
					valueElem.appendChild(highElem);
				}
			}else if(attrMode.startsWith(LESS_THAN)){
				if(attrMode.equals(LESS_THAN)){
					valueElem.setAttribute("highClosed", "false");
				}
				if(isRadiation || isResult){//for radiation dosage and radiation duration
					Element highElem = dataCriteriaXMLProcessor.getOriginalDoc()
							.createElement(LOW);
					highElem.setAttribute(NULL_FLAVOR, "NINF");
					valueElem.appendChild(highElem);
				}
				Element highElem = dataCriteriaXMLProcessor.getOriginalDoc()
						.createElement(HIGH);
				if(isResult){
					highElem.setAttribute(XSI_TYPE, "PQ");
				}
				highElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem("comparisonValue").getNodeValue());
				if(unitAttrib != null){
					String unitString = getUnitString(unitAttrib.getNodeValue());
					highElem.setAttribute("unit", unitString);
				}
				valueElem.appendChild(highElem);
			}
		}
		
		
		return valueElem;
	}
	
	/**
	 * Adds the target site or priority code or route code element.
	 *
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param attributeQDMNode the attribute qdm node
	 * @param templateNode the template node
	 */
	private void addTargetSiteOrPriorityCodeOrRouteCodeElement(Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor,
			Node attributeQDMNode, Node templateNode) {
		String targetElementName = templateNode.getAttributes().getNamedItem("target").getNodeValue();
		Element targetSiteCodeElement = dataCriteriaXMLProcessor.getOriginalDoc()
				.createElement(targetElementName);
		String insertBeforeNodeName = null;
		String insertAfterNodeName = null;
		if (templateNode.getAttributes().getNamedItem("insertBeforeNode") != null) {
			insertBeforeNodeName = templateNode.getAttributes().getNamedItem("insertBeforeNode").getNodeValue();
		} else if (templateNode.getAttributes().getNamedItem("insertAfterNode") != null) {
			insertAfterNodeName = templateNode.getAttributes().getNamedItem("insertAfterNode").getNodeValue();
		}
		if (templateNode.getAttributes().getNamedItem("childTarget") != null) {
			String qdmOidValue = attributeQDMNode.getAttributes().getNamedItem(OID)
					.getNodeValue();
			String version = attributeQDMNode.getAttributes().getNamedItem("version")
					.getNodeValue();
			Element valueElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(ITEM);
			valueElem.setAttribute("valueSet", qdmOidValue);
			addValueSetVersion(attributeQDMNode, valueElem);
			Element displayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(DISPLAY_NAME);
			displayNameElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem(NAME).getNodeValue()
					+ " " + attributeQDMNode.getAttributes().getNamedItem(TAXONOMY).getNodeValue() + " Value Set");
			valueElem.appendChild(displayNameElem);
			targetSiteCodeElement.appendChild(valueElem);
			if (insertBeforeNodeName != null) {
				Node outBoundElement =  dataCriteriaElem.getElementsByTagName(insertBeforeNodeName).item(0);
				if (outBoundElement != null) {
					Node parentOfOutBoundElement = outBoundElement.getParentNode();
					parentOfOutBoundElement.insertBefore(targetSiteCodeElement, outBoundElement);
				} else {
					dataCriteriaElem.appendChild(targetSiteCodeElement);
				}
			} else if (insertAfterNodeName != null) {
				Node outBoundElement =  dataCriteriaElem.getElementsByTagName(insertAfterNodeName).item(0).getNextSibling();
				if (outBoundElement != null) {
					outBoundElement.getParentNode().insertBefore(targetSiteCodeElement, outBoundElement);
				} else {
					dataCriteriaElem.appendChild(targetSiteCodeElement);
				}
			} else {
				dataCriteriaElem.appendChild(targetSiteCodeElement);
			}
		} else if (templateNode.getAttributes().getNamedItem(FLAVOR_ID) != null) {
			String flavorIdValue = templateNode.getAttributes().getNamedItem(FLAVOR_ID).getNodeValue();
			targetSiteCodeElement.setAttribute(FLAVOR_ID, flavorIdValue);
			if (insertBeforeNodeName != null) {
				Node outBoundElement =  dataCriteriaElem.getElementsByTagName(insertBeforeNodeName).item(0);
				if (outBoundElement != null) {
					Node parentOfOutBoundElement = outBoundElement.getParentNode();
					parentOfOutBoundElement.insertBefore(targetSiteCodeElement, outBoundElement);
				} else {
					dataCriteriaElem.appendChild(targetSiteCodeElement);
				}
			} else if (insertAfterNodeName != null) {
				Node outBoundElement =  dataCriteriaElem.getElementsByTagName(insertAfterNodeName).item(0).getNextSibling();
				if (outBoundElement != null) {
					outBoundElement.getParentNode().insertBefore(targetSiteCodeElement, outBoundElement);
				} else {
					dataCriteriaElem.appendChild(targetSiteCodeElement);
				}
			} else {
				dataCriteriaElem.appendChild(targetSiteCodeElement);
			}
		} else if (templateNode.getAttributes().getNamedItem("addValueSet") != null) {
			String qdmOidValue = attributeQDMNode.getAttributes().getNamedItem(OID)
					.getNodeValue();
			targetSiteCodeElement.setAttribute("valueSet", qdmOidValue);
			addValueSetVersion(attributeQDMNode, targetSiteCodeElement);
			Element displayNameElem = dataCriteriaXMLProcessor.getOriginalDoc()
					.createElement(DISPLAY_NAME);
			displayNameElem.setAttribute(VALUE, attributeQDMNode.getAttributes().getNamedItem(NAME).getNodeValue()
					+ " " + attributeQDMNode.getAttributes().getNamedItem(TAXONOMY).getNodeValue() + " Value Set");
			targetSiteCodeElement.appendChild(displayNameElem);
			if (insertBeforeNodeName != null) {
				Node outBoundElement =  dataCriteriaElem.getElementsByTagName(insertBeforeNodeName).item(0);
				if (outBoundElement != null) {
					Node parentOfOutBoundElement = outBoundElement.getParentNode();
					parentOfOutBoundElement.insertBefore(targetSiteCodeElement, outBoundElement);
				} else {
					dataCriteriaElem.appendChild(targetSiteCodeElement);
				}
			} else {
				dataCriteriaElem.appendChild(targetSiteCodeElement);
			}
		}
	}
	
	/**
	 * Method to generate HQMF XML for date time attributes.
	 *
	 * @param childNode the child node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param simpleXmlprocessor the simple xmlprocessor
	 * @param attributeQDMNode the attribute qdm node
	 */
	private void generateDateTimeAttributes(Node childNode,
			Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor,
			XmlProcessor simpleXmlprocessor, Node attributeQDMNode) {
		
		Element effectiveTimeNode = dataCriteriaXMLProcessor.getOriginalDoc().createElement(EFFECTIVE_TIME);
		effectiveTimeNode.setAttribute(XSI_TYPE, "IVL_TS");
		generateDateTimeAttributesTag(effectiveTimeNode, attributeQDMNode, dataCriteriaElem, dataCriteriaXMLProcessor, false);
	}
	
	
	/**
	 * Generate date time attributes tag.
	 *
	 * @param dateTimeNode the effective time node
	 * @param attributeQDMNode the attribute qdm node
	 * @param dataCriteriaElem the data criteria elem
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param isOrder the is order
	 */
	private void generateDateTimeAttributesTag(Node dateTimeNode, Node attributeQDMNode,
			Element dataCriteriaElem, XmlProcessor dataCriteriaXMLProcessor, boolean isOrder) {
		
		String attrName = (String) attributeQDMNode.getUserData(ATTRIBUTE_NAME);
		String attrMode = (String) attributeQDMNode.getUserData(ATTRIBUTE_MODE);
		String attrDate = (String) attributeQDMNode.getUserData(ATTRIBUTE_DATE);
		
		String timeTagName = "";
		if (attrName.equals(START_DATETIME)
				|| attrName.equalsIgnoreCase(FACILITY_LOCATION_ARRIVAL_DATETIME)
				|| ADMISSION_DATETIME.equalsIgnoreCase(attrName)
				|| ACTIVE_DATETIME.equalsIgnoreCase(attrName)
				|| DATE.equalsIgnoreCase(attrName)
				|| TIME.equalsIgnoreCase(attrName)
				|| INCISION_DATETIME.equalsIgnoreCase(attrName)) {
			timeTagName = LOW;
		} else if (attrName.equals(STOP_DATETIME)
				|| attrName.equalsIgnoreCase(FACILITY_LOCATION_DEPARTURE_DATETIME)
				|| attrName.equalsIgnoreCase(DISCHARGE_DATETIME)
				|| attrName.equalsIgnoreCase(REMOVAL_DATETIME)
				|| attrName.equalsIgnoreCase(SIGNED_DATETIME)) {
			timeTagName = HIGH;
		}
		
		if(CHECK_IF_PRESENT.equals(attrMode)){
			
			if(timeTagName.length() > 0){
				Element timeTagNode = dataCriteriaElem.getOwnerDocument().createElement(timeTagName);
				timeTagNode.setAttribute(FLAVOR_ID, "ANY.NONNULL");
				dateTimeNode.appendChild(timeTagNode);
			}
		}else if(attrMode.startsWith(Generator.LESS_THAN) || attrMode.startsWith(Generator.GREATER_THAN) || attrMode.equals(Generator.EQUAL_TO)){
			
			if(attrMode.equals(Generator.EQUAL_TO)){
				if(timeTagName.length() > 0){
					Element timeTagNode = dataCriteriaElem.getOwnerDocument().createElement(timeTagName);
					timeTagNode.setAttribute(VALUE, attrDate);
					dateTimeNode.appendChild(timeTagNode);
				}
			}else if(attrMode.startsWith(Generator.GREATER_THAN)){
				if(timeTagName.length() > 0){
					Element timeTagNode = dataCriteriaElem.getOwnerDocument().createElement(timeTagName);
					Element uncertainRangeNode = dataCriteriaElem.getOwnerDocument().createElement("uncertainRange");
					if(attrMode.equals(Generator.GREATER_THAN)){
						uncertainRangeNode.setAttribute("lowClosed", "false");
					}
					Element lowNode = dataCriteriaElem.getOwnerDocument().createElement(LOW);
					lowNode.setAttribute(XSI_TYPE, "TS");
					lowNode.setAttribute(VALUE, attrDate);
					
					Element highNode = dataCriteriaElem.getOwnerDocument().createElement(HIGH);
					highNode.setAttribute(XSI_TYPE, "TS");
					highNode.setAttribute("nullFlavor", "PINF");
					
					uncertainRangeNode.appendChild(lowNode);
					uncertainRangeNode.appendChild(highNode);
					timeTagNode.appendChild(uncertainRangeNode);
					dateTimeNode.appendChild(timeTagNode);
				}
			}else if(attrMode.startsWith(Generator.LESS_THAN)){
				if(timeTagName.length() > 0){
					Element timeTagNode = dataCriteriaElem.getOwnerDocument().createElement(timeTagName);
					Element uncertainRangeNode = dataCriteriaElem.getOwnerDocument().createElement("uncertainRange");
					if(attrMode.equals(Generator.LESS_THAN)){
						uncertainRangeNode.setAttribute("highClosed", "false");
					}
					Element lowNode = dataCriteriaElem.getOwnerDocument().createElement(LOW);
					lowNode.setAttribute(XSI_TYPE, "TS");
					lowNode.setAttribute("nullFlavor", "NINF");
					
					Element highNode = dataCriteriaElem.getOwnerDocument().createElement(HIGH);
					highNode.setAttribute(XSI_TYPE, "TS");
					highNode.setAttribute(VALUE, attrDate);
					
					uncertainRangeNode.appendChild(lowNode);
					uncertainRangeNode.appendChild(highNode);
					timeTagNode.appendChild(uncertainRangeNode);
					dateTimeNode.appendChild(timeTagNode);
				}
			}
		}
		
		/**
		 * If effectiveTimeNode has any child nodes then add it to the main dataCriteriaNode.
		 */
		if(dateTimeNode.hasChildNodes()){
			
			if(attrName.equalsIgnoreCase(START_DATETIME)
					||attrName.equalsIgnoreCase(STOP_DATETIME)
					||attrName.equalsIgnoreCase(SIGNED_DATETIME)){
				NodeList nodeList = dataCriteriaElem.getElementsByTagName("participation");
				if ((nodeList != null) && (nodeList.getLength() > 0) && isOrder) {
					if(nodeList.getLength()>1){
						nodeList.item(1).insertBefore(dateTimeNode, dataCriteriaElem.getElementsByTagName("role").item(1));
					} else {
						nodeList.item(0).insertBefore(dateTimeNode, dataCriteriaElem.getElementsByTagName("role").item(0));
					}
				} else {
					NodeList valueNodeList = dataCriteriaElem.getElementsByTagName("value");
					if ((valueNodeList != null) && (valueNodeList.getLength() > 0)) {
						dataCriteriaElem.insertBefore(dateTimeNode, valueNodeList.item(0));
					} else {
						dataCriteriaElem.appendChild(dateTimeNode);
					}
				}
			} else{
				NodeList nodeList = dataCriteriaElem.getElementsByTagName("value");
				if ((nodeList != null) && (nodeList.getLength() > 0)) {
					dataCriteriaElem.insertBefore(dateTimeNode, nodeList.item(0));
				} else {
					
					if (attrName.contains("facility")) {
						NodeList nodeListParticipation =  dataCriteriaElem.getElementsByTagName("role");
						if ((nodeListParticipation != null) && (nodeListParticipation.getLength() > 0)) {
							nodeListParticipation.item(0).getFirstChild().getParentNode().appendChild(dateTimeNode);
						}
					} else if(attrName.equalsIgnoreCase(INCISION_DATETIME)) { //for Incision Datetime Attribute effective Time is Added inside
						NodeList nodeListProcedureCriteria =  dataCriteriaElem.getElementsByTagName("procedureCriteria");
						if ((nodeListProcedureCriteria != null) && (nodeListProcedureCriteria.getLength() > 0)) {
							nodeListProcedureCriteria.item(0).getFirstChild().getParentNode().appendChild(dateTimeNode);
						}
					} else {
						NodeList nodeListParticipation = dataCriteriaElem.getElementsByTagName("participation");
						if ((nodeListParticipation != null) && (nodeListParticipation.getLength() > 0)) {
							dataCriteriaElem.insertBefore(dateTimeNode, nodeListParticipation.item(0));
						} else {
							dataCriteriaElem.appendChild(dateTimeNode);
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * Method to append Facility Location attribute template to data type. This attribute can only have value ser
	 * and Check If present mode's and these are added to code tag.
	 *
	 * @param templateNode the template node
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param templateXMLProcessor the template xml processor
	 * @param dataCriteriaElem the data criteria elem
	 * @param attrNode the attr node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void appendSubTemplateInFacilityAttribute(Node templateNode, XmlProcessor dataCriteriaXMLProcessor, XmlProcessor templateXMLProcessor,
			Element dataCriteriaElem, Node attrNode) throws XPathExpressionException{
		String subTemplateName = templateNode.getAttributes().getNamedItem("includeSubTemplate").getNodeValue();
		Node  subTemplateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
				+ subTemplateName);
		NodeList subTemplateNodeChilds = templateXMLProcessor.findNodeList(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
				+ subTemplateName + "/child::node()");
		if(subTemplateNode.getAttributes().getNamedItem("changeAttribute") != null) {
			Node  attributedToBeChangedInNode = null;
			String[] tagToBeModified = subTemplateNode.getAttributes().getNamedItem("changeAttribute").getNodeValue().split(",");
			for (String changeAttribute : tagToBeModified) {
				attributedToBeChangedInNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
						+ subTemplateName+"//"+changeAttribute);
				if (changeAttribute.equalsIgnoreCase(ID)) {
					Node childNodes = attributedToBeChangedInNode.getFirstChild().getNextSibling();
					String rootId = (String) attrNode.getUserData(ATTRIBUTE_UUID);
					childNodes.getAttributes().getNamedItem("root").
					setNodeValue(rootId);
					childNodes.getAttributes().getNamedItem("extension").
					setNodeValue(StringUtils.deleteWhitespace((String) attrNode.getUserData(ATTRIBUTE_NAME)));
				} else if(changeAttribute.equalsIgnoreCase(CODE)){
					String attrMode = (String) attrNode.getUserData(ATTRIBUTE_MODE);
					if(VALUE_SET.equals(attrMode)){
						if(attributedToBeChangedInNode.hasAttributes()){
							((Element)attributedToBeChangedInNode).removeAttribute("flavorId");
							((Element)attributedToBeChangedInNode).removeAttribute("xsi:type");
						}
						if(attributedToBeChangedInNode.hasChildNodes()){
							((Element)attributedToBeChangedInNode).removeChild(attributedToBeChangedInNode.getFirstChild());
						}
						checkIfSelectedModeIsValueSet(templateXMLProcessor, attrNode, subTemplateNode, (Element)attributedToBeChangedInNode);
					} else if(CHECK_IF_PRESENT.equals(attrMode)){
						if(attributedToBeChangedInNode.hasAttributes()){
							((Element)attributedToBeChangedInNode).removeAttribute("valueSet");
						}
						if(attributedToBeChangedInNode.hasChildNodes()){
							((Element)attributedToBeChangedInNode).removeChild(attributedToBeChangedInNode.getFirstChild());
						}
						checkIfSelectedModeIsPresent(templateXMLProcessor, attrNode, subTemplateNode, (Element)attributedToBeChangedInNode);
						((Element)attributedToBeChangedInNode).removeAttribute("xsi:type");
					}
				}
			}
		}
		for (int i = 0; i < subTemplateNodeChilds.getLength(); i++) {
			Node childNode = subTemplateNodeChilds.item(i);
			Node nodeToAttach = dataCriteriaXMLProcessor.getOriginalDoc().importNode(childNode, true);
			XmlProcessor.clean(nodeToAttach);
			dataCriteriaElem.appendChild(nodeToAttach);
		}
	}
	
	/**
	 * Add SubTemplate defined in Template.xml to data criteria Element.
	 *
	 * @param templateNode - Node
	 * @param dataCriteriaXMLProcessor - XmlProcessor for Data Criteria
	 * @param templateXMLProcessor -XmlProcessor for Template Xml.
	 * @param dataCriteriaElem - Element
	 * @param qdmNode the qdm node
	 * @param attributeQDMNode the attribute qdm node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void appendSubTemplateNode(Node templateNode, XmlProcessor dataCriteriaXMLProcessor, XmlProcessor templateXMLProcessor,
			Element dataCriteriaElem, Node qdmNode , Node attributeQDMNode) throws XPathExpressionException {
		String subTemplateName = templateNode.getAttributes().getNamedItem("includeSubTemplate").getNodeValue();
		Node  subTemplateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
				+ subTemplateName);
		NodeList subTemplateNodeChilds = templateXMLProcessor.findNodeList(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
				+ subTemplateName + "/child::node()");
		String qdmOidValue = qdmNode.getAttributes().getNamedItem(OID)
				.getNodeValue();
		String version = qdmNode.getAttributes().getNamedItem("version")
				.getNodeValue();
		boolean addVersionToValueTag = false;
		if("1.0".equals(version)) {
			if(qdmNode.getAttributes().getNamedItem("effectiveDate") !=null){
				version = qdmNode.getAttributes().getNamedItem("effectiveDate").getNodeValue();
				addVersionToValueTag = true;
			} else {
				addVersionToValueTag = false;
			}
		} else {
			addVersionToValueTag = true;
		}
		String qdmName = qdmNode.getAttributes().getNamedItem(NAME).getNodeValue();
		String qdmNameDataType = qdmNode.getAttributes().getNamedItem("datatype").getNodeValue();
		String qdmTaxonomy = qdmNode.getAttributes().getNamedItem(TAXONOMY).getNodeValue();
		if (subTemplateNode.getAttributes().getNamedItem("changeAttribute") != null) {
			String[] attributeToBeModified = subTemplateNode.getAttributes().
					getNamedItem("changeAttribute").getNodeValue().split(",");
			for (String changeAttribute : attributeToBeModified) {
				Node  attributedToBeChangedInNode = null;
				attributedToBeChangedInNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc()
						, "/templates/subtemplates/" + subTemplateName + "//" + changeAttribute);
				if (changeAttribute.equalsIgnoreCase(ID)) {
					String rootId = qdmNode.getAttributes().getNamedItem("uuid").getNodeValue();
					attributedToBeChangedInNode.getAttributes().getNamedItem("root").
					setNodeValue(rootId);
					attributedToBeChangedInNode.getAttributes().getNamedItem("extension").
					setNodeValue(UUIDUtilClient.uuid());
				} else if (changeAttribute.equalsIgnoreCase(CODE)) {
					attributedToBeChangedInNode.getAttributes().getNamedItem("valueSet").setNodeValue(qdmOidValue);
					if(addVersionToValueTag){
						Attr attrNode = attributedToBeChangedInNode.getOwnerDocument().createAttribute("valueSetVersion");
						attrNode.setNodeValue(version);
						attributedToBeChangedInNode.getAttributes().setNamedItem(attrNode);
					}
					
				} else if (changeAttribute.equalsIgnoreCase(DISPLAY_NAME)) {
					attributedToBeChangedInNode.getAttributes().getNamedItem("value").
					setNodeValue(qdmName + " " + qdmTaxonomy + " value set");
				} else if (changeAttribute.equalsIgnoreCase(TITLE)) {
					attributedToBeChangedInNode.getAttributes().getNamedItem("value").setNodeValue(qdmNameDataType);
				}
			}
		}
		for (int i = 0; i < subTemplateNodeChilds.getLength(); i++) {
			Node childNode = subTemplateNodeChilds.item(i);
			Node nodeToAttach = dataCriteriaXMLProcessor.getOriginalDoc().importNode(childNode, true);
			XmlProcessor.clean(nodeToAttach);
			dataCriteriaElem.appendChild(nodeToAttach);
		}
	}
	
	/**
	 * Append sub template with order attribute.
	 *
	 * @param templateNode the template node
	 * @param dataCriteriaXMLProcessor the data criteria xml processor
	 * @param templateXMLProcessor the template xml processor
	 * @param dataCriteriaElem the data criteria elem
	 * @param qdmNode the qdm node
	 * @param attrNode the attr node
	 * @throws XPathExpressionException the x path expression exception
	 */
	private void appendSubTemplateWithOrderAttribute(Node templateNode, XmlProcessor dataCriteriaXMLProcessor, XmlProcessor templateXMLProcessor,
			Element dataCriteriaElem,Node qdmNode, Node attrNode) throws XPathExpressionException{
		String subTemplateName = templateNode.getAttributes().getNamedItem("includeOtherSubTemplate").getNodeValue();
		Node  subTemplateNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
				+ subTemplateName);
		NodeList subTemplateNodeChilds = templateXMLProcessor.findNodeList(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
				+ subTemplateName + "/child::node()");
		boolean isOrder = templateNode.getAttributes().getNamedItem("typeOrder").getNodeValue()!=null;
		if(subTemplateNode.getAttributes().getNamedItem("changeAttribute") != null) {
			Node  attributedToBeChangedInNode = null;
			String[] tagToBeModified = subTemplateNode.getAttributes().getNamedItem("changeAttribute").getNodeValue().split(",");
			for (String changeAttribute : tagToBeModified) {
				attributedToBeChangedInNode = templateXMLProcessor.findNode(templateXMLProcessor.getOriginalDoc(), "/templates/subtemplates/"
						+ subTemplateName+"//"+changeAttribute);
				if (changeAttribute.equalsIgnoreCase(ID)) {
					Node childNodes = attributedToBeChangedInNode.getFirstChild().getNextSibling();
					String rootId = (String) attrNode.getUserData(ATTRIBUTE_UUID);
					childNodes.getAttributes().getNamedItem("root").
					setNodeValue(rootId);
					childNodes.getAttributes().getNamedItem("extension").
					setNodeValue(StringUtils.deleteWhitespace((String) attrNode.getUserData(ATTRIBUTE_NAME)));
				}
			}
		}
		
		for (int i = 0; i < subTemplateNodeChilds.getLength(); i++) {
			Node childNode = subTemplateNodeChilds.item(i);
			Node nodeToAttach = dataCriteriaXMLProcessor.getOriginalDoc().importNode(childNode, true);
			XmlProcessor.clean(nodeToAttach);
			dataCriteriaElem.appendChild(nodeToAttach);
		}
		Element timeNode = dataCriteriaXMLProcessor.getOriginalDoc().createElement(TIME);
		generateDateTimeAttributesTag(timeNode, attrNode, dataCriteriaElem, dataCriteriaXMLProcessor, isOrder);
	}
	@Override
	public String generate(MeasureExport me) throws Exception {
		
		return null;
	}
	
	/**
	 * @param qdmNode
	 * @param excerptElement
	 * @param dataCriteriaXMLProcessor
	 * @param simpleXmlprocessor
	 * @param attributeQDMNode
	 * @throws XPathExpressionException
	 */
	public void generateAttributeTagForFunctionalOp(Node qdmNode, Element excerptElement
			, XmlProcessor dataCriteriaXMLProcessor, XmlProcessor simpleXmlprocessor
			, Node attributeQDMNode) throws XPathExpressionException{
		createDataCriteriaForAttributes(qdmNode, excerptElement, dataCriteriaXMLProcessor, simpleXmlprocessor, attributeQDMNode);
	}
}
