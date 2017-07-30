package org.prettycat.dataflow;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLProtocol {
	public static final String NAMESPACE = "https://xmlns.zombofant.net/prettycat/1.0/ir-graph";
	
	public static Element createGraphElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "graph");
		return result;
	}
	
	public static Element createMethodElement(
			Document doc,
			String fqmn)
	{
		Element result = doc.createElementNS(NAMESPACE, "method");
		result.setAttribute("id", "java:"+fqmn);
		return result;
	}
	
	public static Element createParametersElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "parameters");
		return result;
	}
	
	public static Element createCDFGElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "cdfg");
		return result;
	}
	
	public static Element createBlockElement(
			Document doc,
			int num)
	{
		Element result = doc.createElementNS(NAMESPACE, "block");
		result.setAttribute("num", ""+num);
		return result;
	}
	
	public static Element createFloatingElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "floating");
		return result;
	}
	
	public static Element createInputElement(
			Document doc,
			String obj)
	{
		Element result = doc.createElementNS(NAMESPACE, "input");
		result.setAttribute("object", obj);
		return result;
	}
	
	public static Element createVirtualElement(
			Document doc,
			String slot,
			String node)
	{
		Element result = doc.createElementNS(NAMESPACE, "virtual");
		result.setAttribute("slot", slot);
		result.setAttribute("node", node);
		return result;
	}
	
	public static Element createSourceElement(
			Document doc,
			String obj,
			int block)
	{
		Element result = doc.createElementNS(NAMESPACE, "source");
		result.setAttribute("object", obj);
		result.setAttribute("block", ""+block);
		return result;
	}
	
	public static Element createFrameStateElement(
			Document doc,
			String obj)
	{
		Element result = doc.createElementNS(NAMESPACE, "frame-state");
		result.setAttribute("object", obj);
		return result;
	}
	
	public static Element createInputsElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "inputs");
		return result;
	}
	
	public static Element createValueElement(
			Document doc,
			String slot,
			String fqtn,
			String node)
	{
		Element result = doc.createElementNS(NAMESPACE, "value");
		result.setAttribute("slot", slot);
		result.setAttribute("type", fqtn);
		result.setAttribute("node", node);
		return result;
	}
	
	public static Element createPhiElement(
			Document doc,
			String slot,
			String node)
	{
		Element result = doc.createElementNS(NAMESPACE, "phi");
		result.setAttribute("slot", slot);
		result.setAttribute("node", node);
		return result;
	}
	
	public static Element createExitElement(
			Document doc,
			int block)
	{
		Element result = doc.createElementNS(NAMESPACE, "exit");
		result.setAttribute("block", ""+block);
		return result;
	}
	
	public static Element createEntryElement(
			Document doc,
			int block)
	{
		Element result = doc.createElementNS(NAMESPACE, "entry");
		result.setAttribute("block", ""+block);
		return result;
	}
	
	public static Element createPassElement(
			Document doc,
			int slot,
			String object)
	{
		Element result = doc.createElementNS(NAMESPACE, "pass");
		result.setAttribute("slot", ""+slot);
		result.setAttribute("object", object);
		return result;
	}
	
	public static Element createCallElement(
			Document doc,
			String fqmn,
			String node)
	{
		Element result = doc.createElementNS(NAMESPACE, "call");
		result.setAttribute("method", "java:"+fqmn);
		result.setAttribute("node", node);
		return result;
	}
	
	public static Element createNodeElement(
			Document doc,
			String node)
	{
		Element result = doc.createElementNS(NAMESPACE, "node");
		result.setAttribute("node", node);
		return result;
	}
	
	public static Element createNodesElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "nodes");
		return result;
	}
	
	public static Element createExitsElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "exits");
		return result;
	}
	
	public static Element createEntriesElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "entries");
		return result;
	}
	
	public static Element createParameterElement(
			Document doc,
			String slot,
			String fqtn)
	{
		Element result = doc.createElementNS(NAMESPACE, "parameter");
		result.setAttribute("slot", slot);
		result.setAttribute("type", "java:"+fqtn);
		return result;
	}
	
	public static Element createThisElement(
			Document doc)
	{
		Element result = doc.createElementNS(NAMESPACE, "this");
		return result;
	}
}
