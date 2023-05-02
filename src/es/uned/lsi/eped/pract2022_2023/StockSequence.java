package es.uned.lsi.eped.pract2022_2023;

import es.uned.lsi.eped.DataStructures.SequenceIF;
import es.uned.lsi.eped.DataStructures.IteratorIF;
import es.uned.lsi.eped.DataStructures.List;

public class StockSequence<E> implements StockIF {

	protected List<StockPair> stock;
	
	/* Constructor de la clase */
	public StockSequence() {
		this.stock = new List<>();
	}
	
	@Override
	public int retrieveStock(String p) {
		StockPair stockElement = searchByProductID(p);
		if(stockElement == null) {
			return -1;
		}
		
		return stockElement.getUnidades();
	}
	
	@Override
	public void updateStock(String p, int u) {
		StockPair existingStockElement = searchByProductID(p);
		
		if (existingStockElement != null) {
			existingStockElement.setUnidades(u);
		} else {
			insertNewStockElement(p, u);
		}
		
	}
	
	@Override
	public SequenceIF<StockPair> listStock(String prefix) {
		if (prefix == "") {
			return this.stock;
		}
		
		List<StockPair> partialStockList = new List<>();
		IteratorIF<StockPair> iterator = this.stock.iterator();
		
		while (iterator.hasNext()) {
			StockPair element = iterator.getNext();
			if (element.getProducto().indexOf(prefix) == 0) {
				partialStockList.insert(partialStockList.size() + 1, element);
			}
		}
		
		return partialStockList;
	}

	
	/*
	 * Busca un nodo por ID de producto
	 */
	private StockPair searchByProductID(String p) {
		IteratorIF<StockPair> iterator = this.stock.iterator();
		while (iterator.hasNext()) {
			StockPair element = iterator.getNext();
			if (element.getProducto().equals(p)) 
				return element;
		}
		
		return null;
	}	
	
	
	/*
	 * Inserta un nuevo nodo, ordenando alfabéticamente por el índice de producto
	 */
	private void insertNewStockElement(String p, int u) {
		
		StockPair newElement = new StockPair(p, u);
			
		// Iterar la lista hasta encontrar la posición en que debe entrar el elemento
		// según orden alfabético de ID, e insertarlo en esta
		IteratorIF<StockPair> iterator = this.stock.iterator();
		int pos = 1;
		boolean inserted = false;
		while (iterator.hasNext() && !inserted) {
			StockPair currentElement = iterator.getNext();
			if (currentElement.getProducto().compareTo(p) > 0) {
				this.stock.insert(pos, newElement);
				inserted = true;
			}
			pos++;
		}
		// Si se termina de recorrer sin haberlo insertado, insertarlo al final
		if (!inserted) {
			this.stock.insert(this.stock.size() + 1, newElement);
			inserted = true;
		}
	}	
}
