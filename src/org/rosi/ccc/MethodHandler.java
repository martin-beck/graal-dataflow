package org.rosi.ccc;

import java.util.ArrayList;

import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.nodeinfo.Verbosity;
import org.graalvm.compiler.nodes.CallTargetNode;
import org.graalvm.compiler.nodes.InvokeNode;
import org.graalvm.compiler.nodes.InvokeWithExceptionNode;
import org.graalvm.compiler.nodes.MergeNode;
import org.graalvm.compiler.nodes.ParameterNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.VirtualState;
import org.graalvm.compiler.nodes.cfg.Block;
import org.graalvm.compiler.nodes.cfg.ControlFlowGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

public class MethodHandler {
	public class XMLGenerator {
		private Element makeParametersElement(Document doc)
		{
			Element parameters = XMLProtocol.createParametersElement(doc);
			ResolvedJavaType accessing_class = m_method.getDeclaringClass();
			int i = 0;
			for (JavaType type: m_method.toParameterTypes()) {
				ResolvedJavaType resolved_type = type.resolve(accessing_class);
				if (resolved_type.equals(accessing_class) && i == 0) {
					parameters.appendChild(XMLProtocol.createThisElement(doc));
				} else {
					parameters.appendChild(XMLProtocol.createParameterElement(doc, i, resolved_type.toClassName()));
				}
				i += 1;
			}
			return parameters;
		}
		
		private String getNodeSlot(Node node) {
			if (node instanceof ParameterNode) {
				ParameterNode param_node = (ParameterNode)node;
				return "param:"+param_node.index();
			} else {
				return "local:"+node.toString(Verbosity.Id);
			}
		}
		
		private Element makeValueNodeElement(Document doc, ValueNode node)
		{
			Element result = XMLProtocol.createValueElement(doc, Integer.parseInt(node.toString(Verbosity.Id)), "-", node.toString());
			if (node.inputs().isNotEmpty()) {
				result.appendChild(makeInputsElement(doc, node));
			}
			return result;
		}
		
		private Element makeVirtualNodeElement(Document doc, VirtualState node)
		{
			Element result = XMLProtocol.createVirtualElement(doc, Integer.parseInt(node.toString(Verbosity.Id)), node.toString());
			if (node.inputs().isNotEmpty()) {
				result.appendChild(makeInputsElement(doc, node));
			}
			return result;
		}
		
		private Element makeMergeNodeElement(Document doc, MergeNode node)
		{
			Element result = XMLProtocol.createPhiElement(doc, Integer.parseInt(node.toString(Verbosity.Id)));
			ArrayList<Node> inputs = new ArrayList<>();
			node.inputs().forEach(inputs::add);
			Node frame_state = inputs.get(0);
			inputs.remove(0);
			
			result.appendChild(XMLProtocol.createFrameStateElement(doc, getNodeSlot(frame_state)));
			
			for (Node input: inputs) {
				result.appendChild(XMLProtocol.createSourceElement(doc, getNodeSlot(input), m_cfg.blockFor(input).getId()));
			}
			
			return result;
		}
		
		private Element makeCallNodeElement(Document doc, Node node, CallTargetNode target)
		{
			Element result = XMLProtocol.createCallElement(
					doc, 
					getFullyQualifiedMethodName(target.targetMethod())
					);
			int i = 0;
			for (Node parameter: target.inputs()) {
				result.appendChild(XMLProtocol.createPassElement(doc, i, getNodeSlot(parameter)));
				i += 1;
			}
			return result;
		}
		
		private Element makeGenericNodeElement(Document doc, Node node)
		{
			Element result = XMLProtocol.createNodeElement(doc, node.toString());
			if (node.inputs().isNotEmpty()) {
				result.appendChild(makeInputsElement(doc, node));
			}
			return result;
		}
		
		private Element makeNodeElement(Document doc, Node node)
		{
			Element result = null;
			System.out.println(node.getClass());
			if (node instanceof InvokeNode) {
				result = makeCallNodeElement(doc, node, ((InvokeNode)node).callTarget());
			} else if (node instanceof InvokeWithExceptionNode) {
				result = makeCallNodeElement(doc, node, ((InvokeWithExceptionNode)node).callTarget());
			} else if (node instanceof MergeNode) {
				result = makeMergeNodeElement(doc, (MergeNode)node);
			} else if (node instanceof ValueNode) {
				result = makeValueNodeElement(doc, (ValueNode)node);
			} else if (node instanceof VirtualState) {
				result = makeVirtualNodeElement(doc, (VirtualState)node);
			} else {
				result = makeGenericNodeElement(doc, node);
			}
			return result;
		}
		
		private Element makeBlockElement(Document doc, Block bb)
		{
			Element block = XMLProtocol.createBlockElement(
					doc,
					bb.getId());
			Element nodes = XMLProtocol.createNodesElement(doc);
			Node node = bb.getBeginNode();
			Node end = bb.getEndNode();
			while (node != null) {
				nodes.appendChild(makeNodeElement(doc, node));
				if (node.successors().count() != 1) {
					assert(node == end);
					break;
				}
				node = node.successors().first();	
			}
			
			block.appendChild(nodes);
			
			Element exits = XMLProtocol.createExitsElement(doc);
			for (Node successor: end.successors()) {
				Block successor_block = m_cfg.blockFor(successor);
				exits.appendChild(XMLProtocol.createExitElement(
						doc,
						successor_block.getId()
						));
			}
			
			block.appendChild(exits);
			
			return block;
		}
		
		private Element makeInputsElement(Document doc, Node node)
		{
			Element inputs = XMLProtocol.createInputsElement(doc);
			for (Node input_node: node.inputs()) {
				int block_id = -1;
				Block block = m_cfg.blockFor(input_node);
				if (block != null) {
					block_id = block.getId();
				}
				Element input = XMLProtocol.createInputElement(
						doc,
						getNodeSlot(input_node));
				inputs.appendChild(input);
			}
			return inputs;
		}
		
		private Element makeCDFGElement(Document doc)
		{
			Element cdfg = XMLProtocol.createCDFGElement(doc);
			Element floating = XMLProtocol.createFloatingElement(doc);
			for (Node node: m_graph.getNodes()) {
				if (m_cfg.blockFor(node) != null || (node instanceof ParameterNode) || (node instanceof CallTargetNode)) {
					continue;
				}
				floating.appendChild(makeNodeElement(doc, node));
			}
			cdfg.appendChild(floating);
			for (Block bb: m_cfg.getBlocks()) {
				cdfg.appendChild(makeBlockElement(doc, bb));
			}
			return cdfg;
		}
		
		public Element makeElement(Document doc)
		{
			Element result = XMLProtocol.createMethodElement(doc, getFullyQualifiedMethodName());
			result.appendChild(makeParametersElement(doc));
			result.appendChild(makeCDFGElement(doc));
			return result;
		}
	}

	private final ResolvedJavaMethod m_method;
	private final StructuredGraph m_graph;
	private final ControlFlowGraph m_cfg;
	
	public MethodHandler(AnalysisContext context, ResolvedJavaMethod method)
	{
		m_method = method;
		m_graph = context.getStructuredGraphFor(method);
		m_cfg = ControlFlowGraph.compute(m_graph, true, true, true, true);
	}
	
	public ResolvedJavaMethod getMethod()
	{
		return m_method;
	}
	
	public StructuredGraph getGraph()
	{
		return m_graph;
	}
	
	public ControlFlowGraph getCFG()
	{
		return m_cfg;
	}
	
	public XMLGenerator newXMLGenerator()
	{
		return new XMLGenerator();
	}
	
	public static String getFullyQualifiedMethodName(ResolvedJavaMethod method)
	{
		return method.getDeclaringClass().toClassName() + "." + method.getName();
	}
	
	public String getFullyQualifiedMethodName()
	{
		return getFullyQualifiedMethodName(m_method);
	}
}
