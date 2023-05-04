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
		StockPair stockElement = null;
		
		IteratorIF<StockPair> iterator = this.stock.iterator();
		boolean found = false;
		while (iterator.hasNext() && !found) {
			StockPair element = iterator.getNext();
			if (element.getProducto().equals(p)) {
				stockElement = element;
				found = true;
			}			
		}
		
		if(stockElement == null) {
			return -1;
		}
		
		return stockElement.getUnidades();
	}
	
	@Override
	public void updateStock(String p, int u) {
		
		IteratorIF<StockPair> iterator = this.stock.iterator();
		boolean found = false;
		boolean inserted = false;
		int pos = 1;
		while (iterator.hasNext() && !found && !inserted) {
			StockPair element = iterator.getNext();
			if (element.getProducto().equals(p)) {
				element.setUnidades(u);
				found = true;
			} else if (element.getProducto().compareTo(p) > 0) {
				StockPair newElement = new StockPair(p, u);
				this.stock.insert(pos, newElement);
				inserted = true;
			}
			pos++;
		}
		
		if (!found && !inserted) {
			StockPair newElement = new StockPair(p, u);
			this.stock.insert(this.stock.size() + 1, newElement);
			inserted = true;
		}
		
	}
	
	@Override
	public SequenceIF<StockPair> listStock(String prefix) {
		if (prefix.length() == 0) {
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
}
