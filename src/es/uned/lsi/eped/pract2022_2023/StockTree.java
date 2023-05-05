package es.uned.lsi.eped.pract2022_2023;

import es.uned.lsi.eped.DataStructures.GTree;
import es.uned.lsi.eped.DataStructures.GTreeIF;
import es.uned.lsi.eped.DataStructures.IteratorIF;
import es.uned.lsi.eped.DataStructures.List;
import es.uned.lsi.eped.DataStructures.ListIF;
import es.uned.lsi.eped.DataStructures.Queue;
import es.uned.lsi.eped.DataStructures.SequenceIF;

public class StockTree implements StockIF {

	protected GTreeIF<Node> stock; /* El stock es un árbol general de nodos */
	
	/* Constructor de la clase */
	public StockTree() {
		this.stock = new GTree<>();
		this.stock.setRoot(new NodeRoot());
	}

	@Override
	public int retrieveStock(String p) {
		
		int units = 0;
		
		// Obtenemos cola auxiliar con los nodos
		// a leer en el árbol
		Queue<Node> queue = this.getAuxQueue(p);
		
		// leemos recursivamente en el árbol los nodos de la cola
		GTreeIF<Node> tree = this.readPath(this.stock, queue, false);
		
		if (tree == null) {
			units = -1;
		} else {			
			GTreeIF<Node> firstChild = tree.getChildren().get(1);
			if (firstChild == null || firstChild.getRoot().getNodeType() != Node.NodeType.INFO) {
				units = -1;
			} else {
				NodeInfo node = (NodeInfo) firstChild.getRoot();
				units = node.getUnidades(); 
			}	
		}
		
		return units;
	}
	
	@Override
	public void updateStock(String p, int u) {
		// Obtenemos cola auxiliar con los nodos
		// preparados para el árbol
		Queue<Node> queue = this.getAuxQueue(p);
		
		// leemos o insertamos recursivamente en el árbol los nodos de la cola
		GTreeIF<Node> tree = this.readPath(this.stock, queue, true);
		
		NodeInfo node = new NodeInfo(u);
		this.updateInfoChild(tree, node);
	}

	@Override
	public SequenceIF<StockPair> listStock(String prefix) {
		ListIF<StockPair> stockList = new List<StockPair>();
		
		GTreeIF<Node> startTree;
		if (prefix.length() > 0) {
			Queue<Node> queue = this.getAuxQueue(prefix);
			startTree = this.readPath(this.stock, queue, false);
		} else {
			startTree = this.stock;
		}
		
		StringBuilder strBuilder = new StringBuilder(prefix);
		if (startTree != null) {
			this.listChildren(startTree, stockList, strBuilder);
		}
		
		return stockList;
	}
	
	private GTreeIF<Node> readPath(GTreeIF<Node> parentTree, Queue<Node> queue, boolean insertIfNotFound) {
		GTreeIF<Node> targetTree;
		
		Node node = queue.getFirst();		
		queue.dequeue();
		
		GTreeIF<Node> child = insertIfNotFound ? this.getOrCreateChild(parentTree, node) : this.getChild(parentTree, node);
		
		if (child == null || queue.isEmpty()) {
			targetTree = child;
		} else {
			targetTree = this.readPath(child, queue, insertIfNotFound);
		}
		
		return targetTree;
	}	
	
	private void listChildren(GTreeIF<Node> parentTree, ListIF<StockPair> stockList, StringBuilder strBuilder) {
		ListIF<GTreeIF<Node>> children = parentTree.getChildren();
		
		if (children.size() != 0) {
			IteratorIF<GTreeIF<Node>> iterator = children.iterator();

			while (iterator.hasNext()) {
				GTreeIF<Node> currentChild = iterator.getNext();
					
				if (currentChild.getRoot().getNodeType() == Node.NodeType.INFO) {
					// si es NodeInfo, crear StockPair y añadir a stockList
					NodeInfo node = (NodeInfo) currentChild.getRoot();
					
					String product = strBuilder.toString(); 
					int units = node.getUnidades();
					
					StockPair stockPair = new StockPair(product, units);
					
					stockList.insert(stockList.size() + 1, stockPair);
				} else {
					// si es NodeInner, añadir caracter a cadena
					NodeInner node = (NodeInner) currentChild.getRoot();
					
					// copia de strBuilder para mantener recorrido correcto en cada recursión por hijos
					StringBuilder strBuilderCopy = new StringBuilder(strBuilder);
					
					strBuilderCopy.append(node.getLetter());
					
					// leer recursivamente hijos de currentChild
					this.listChildren(currentChild, stockList, strBuilderCopy);
				}		
			}
		}
	}

	
	/*
	 * Prepara cola de nodos a leer
	 */	
	private Queue<Node> getAuxQueue(String p) {
		Queue<Node> queue = new Queue<Node>();
		for (int i = 0; i < p.length(); i++) {
			queue.enqueue(new NodeInner(p.charAt(i)));
		}
		
		return queue;
	}

	private GTreeIF<Node> getChild(GTreeIF<Node> parentTree, Node node) {
		ListIF<GTreeIF<Node>> children = parentTree.getChildren();
		if (children.size() != 0) {
			IteratorIF<GTreeIF<Node>> iterator = children.iterator();

			while (iterator.hasNext()) {
				GTreeIF<Node> currentChild = iterator.getNext();
				
				try {  // controlar posible error lanzado por compareNodes
					if (this.compareNodes(currentChild.getRoot(), node) == 0) {
						return currentChild;
					}
				} catch (Exception e) {
					// no hacer nada, dejar que continúe el bucle
				}
				
			}
		}
		
		return null;
	}
	
	private GTreeIF<Node> getOrCreateChild(GTreeIF<Node> tree, Node node) {

		GTreeIF<Node> child = null;
		ListIF<GTreeIF<Node>> children = tree.getChildren();
		if (children.size() == 0) {
			child = this.createFirstChild(tree, node);
		} else {
			IteratorIF<GTreeIF<Node>> iterator = children.iterator();
			boolean found = false;
			boolean inserted = false;
			int pos = 1;
			while (iterator.hasNext() && !found && !inserted) {
				GTreeIF<Node> currentChild = iterator.getNext();
				try {  // controlar posible error lanzado por compareNodes
					int compareNodes = this.compareNodes(currentChild.getRoot(), node);
					if (compareNodes == 0) {
						child = currentChild;
						found = true;
					} else if (compareNodes > 0){
						GTreeIF<Node> childTree = new GTree<Node>();
						childTree.setRoot(node);
						tree.addChild(pos, childTree);
						child = childTree;
						inserted = true;
					}
				} catch (Exception e) {
					// no hacer nada, dejar que continúe el bucle
				}
				pos++;
			}
			
			if (!found && !inserted) {
				GTreeIF<Node> childTree = new GTree<Node>();
				childTree.setRoot(node);
				tree.addChild(pos, childTree);
				child = childTree;
				inserted = true;
			}
		}
		
		return child;
	}
	
	
	private GTreeIF<Node> updateInfoChild(GTreeIF<Node> parentTree, Node node) {
		
		GTreeIF<Node> child = null;
				
		ListIF<GTreeIF<Node>> children = parentTree.getChildren();
		
		if (children.size() == 0) {
			child = this.createFirstChild(parentTree, node);
		} else {
			GTreeIF<Node> firstChild = children.get(1);
			if (firstChild.getRoot().getNodeType() != Node.NodeType.INFO) {
				child = this.createFirstChild(parentTree, node);
			} else {
				child = firstChild;
				child.setRoot(node);
			}	
		}
		
		return child;
	}
	
	private GTreeIF<Node> createFirstChild(GTreeIF<Node> parentTree, Node node) {
		GTreeIF<Node> childTree = new GTree<Node>();
		childTree.setRoot(node);
		parentTree.addChild(1, childTree);
		
		return childTree;
	}
	
	/*
	 * Compara objetos Node según su subtipo, devolviendo un int 
	 * tal que, si es negativo, subjectNode es menor que referenceNode,
	 * si es 0 son iguales, y si es positivo subjectNode es mayor que
	 * referenceNode. Para el caso de nodos NodeInner, atiende al orden alfabético
	 * de los caracteres contenidos para hacer esta comparación.
	 */
	private int compareNodes(Node subjectNode, Node referenceNode) throws Exception {
		if (subjectNode.getNodeType() != referenceNode.getNodeType()) {
			throw new Exception("Nodes are not of the same type");
		}
		
		if (subjectNode.getNodeType() == Node.NodeType.ROOT) {
			throw new Exception("Nodes of type ROOT cannot be compared");
		}
		
		int compare = 0;
		
		if (subjectNode.getNodeType() == Node.NodeType.INNER) {
			NodeInner subjectNodeInner = (NodeInner) subjectNode;
			NodeInner referenceNodeInner = (NodeInner) referenceNode;
			compare = Character.compare(subjectNodeInner.getLetter(), referenceNodeInner.getLetter());
		} else if (subjectNode.getNodeType() == Node.NodeType.INFO) {
			NodeInfo subjectNodeInfo = (NodeInfo) subjectNode;
			NodeInfo referenceNodeInfo = (NodeInfo) referenceNode;
			compare = subjectNodeInfo.getUnidades() - referenceNodeInfo.getUnidades();
		}
		
		return compare;
	}
	
}
