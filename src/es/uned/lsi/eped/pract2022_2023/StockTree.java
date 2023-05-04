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
		Queue<Node> queue = getAuxQueue(p);
		
		// leemos recursivamente en el árbol los nodos de la cola
		GTreeIF<Node> tree = readPath(this.stock, queue, false);
		
		if (tree == null) {
			units = -1;
		} else {
			GTreeIF<Node> infoChild = getInfoChild(tree);
			if (infoChild == null) {
				units = -1;
			} else {
				NodeInfo node = (NodeInfo) infoChild.getRoot();
				units = node.getUnidades(); 
			}	
		}
		
		return units;
	}
	
	@Override
	public void updateStock(String p, int u) {
		// Obtenemos cola auxiliar con los nodos
		// preparados para el árbol
		Queue<Node> queue = getAuxQueue(p);
		
		// leemos o insertamos recursivamente en el árbol los nodos de la cola
		GTreeIF<Node> tree = readPath(this.stock, queue, true);
		
		NodeInfo node = new NodeInfo(u);
		this.updateInfoChild(tree, node);
	}

	@Override
	public SequenceIF<StockPair> listStock(String prefix) {
		ListIF<StockPair> stockList = new List<StockPair>();
		
		GTreeIF<Node> startTree;
		if (prefix.length() > 0) {
			Queue<Node> queue = getAuxQueue(prefix);
			startTree = readPath(this.stock, queue, false);
		} else {
			startTree = this.stock;
		}
		
		StringBuilder strBuilder = new StringBuilder(prefix);
		if (startTree != null) {
			readChildren(startTree, stockList, strBuilder);
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
			targetTree = readPath(child, queue, insertIfNotFound);
		}
		
		return targetTree;
	}	
	
	private void readChildren(GTreeIF<Node> parentTree, ListIF<StockPair> stockList, StringBuilder strBuilder) {
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
					readChildren(currentChild, stockList, strBuilderCopy);
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

	
	private GTreeIF<Node> getOrCreateChild(GTreeIF<Node> tree, Node node) {
		
		GTreeIF<Node> child = null;
		
		if (node.getNodeType() == Node.NodeType.INFO) {
			// si es NodeInfo, actualizar unidades
			child = this.updateInfoChild(tree, node);
		} else {
			child = this.getChild(tree, node);
			if (child == null) {
				child = this.createChild(tree, node);
			} 
		}
		
		return child;
	}
	
	private GTreeIF<Node> getChild(GTreeIF<Node> parentTree, Node node) {
		ListIF<GTreeIF<Node>> children = parentTree.getChildren();
		if (children.size() != 0) {
			IteratorIF<GTreeIF<Node>> iterator = children.iterator();

			while (iterator.hasNext()) {
				GTreeIF<Node> currentChild = iterator.getNext();
				
				try {  // controlar posible error lanzado por compareNodes
					if (compareNodes(currentChild.getRoot(), node) == 0) {
						return currentChild;
					}
				} catch (Exception e) {
					// no hacer nada, dejar que continúe el bucle
				}
				
			}
		}
		
		return null;
	}
	
	private GTreeIF<Node> createChild(GTreeIF<Node> parentTree, Node node) {
		
		GTreeIF<Node> childTree = new GTree<Node>();
		childTree.setRoot(node);
		
		ListIF<GTreeIF<Node>> children = parentTree.getChildren();
		if (children.size() == 0 || node.getNodeType() == Node.NodeType.INFO) {
			parentTree.addChild(1, childTree);
		} else {
			IteratorIF<GTreeIF<Node>> iterator = children.iterator();
			
			boolean inserted = false;
			int i = 1;
			while (iterator.hasNext() && !inserted) {
				GTreeIF<Node> currentChild = iterator.getNext();
				
				try { // controlar posible error lanzado por compareNodes
					if (compareNodes(node, currentChild.getRoot()) < 0) {
						parentTree.addChild(i, childTree);
						inserted = true;
					} 
				} catch (Exception e){
					// no hacer nada, dejar que continúe el bucle 
				}
				
				i++;
			}
			
			if (!inserted) {
				parentTree.addChild(i, childTree);
				inserted = true;
			}
		}
	
		
		return childTree;
	}
	
	private GTreeIF<Node> createChild(GTreeIF<Node> parentTree, Node node, int position) {
		GTreeIF<Node> childTree = new GTree<Node>();
		childTree.setRoot(node);
		parentTree.addChild(1, childTree);
		
		return childTree;
	}
	
	private GTreeIF<Node> getInfoChild(GTreeIF<Node> parentTree) {
		
		ListIF<GTreeIF<Node>> children = parentTree.getChildren();
		
		if (children.size() != 0) {			

			IteratorIF<GTreeIF<Node>> iterator = children.iterator();
			
			while (iterator.hasNext()) {
				
				GTreeIF<Node> currentChild = iterator.getNext();
				if (currentChild.getRoot().getNodeType() == Node.NodeType.INFO) {
			
					return currentChild;
					
				}
			}
		}
		
		return null;
	}


	private GTreeIF<Node> updateInfoChild(GTreeIF<Node> parentTree, Node node) {
		GTreeIF<Node> child = null;
		
		ListIF<GTreeIF<Node>> children = parentTree.getChildren();
		if (children.size() == 0) {
			
			child = this.createChild(parentTree, node, 1);
		} else {
			IteratorIF<GTreeIF<Node>> iterator = children.iterator();
			boolean found = false;
			while (iterator.hasNext() && !found) {
				GTreeIF<Node> currentChild = iterator.getNext();
				if (currentChild.getRoot().getNodeType() == Node.NodeType.INFO) {
					found = true;
					child = currentChild;
					child.setRoot(node);
				}
			}
			if (!found) {
				child = this.createChild(parentTree, node, 1);
			}
		}
		
		return child;
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
