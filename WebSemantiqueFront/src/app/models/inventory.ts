export interface InventoryResponse {
    results: Inventory[];
}

export interface Inventory {
    inventory: string;       // URI like "http://rescuefood.org/ontology#Inventory..."
    currentQuantity: number; // String because API returns numbers with dots and commas like "500.0" or "15,000000"
    foodId: string;           // URI or empty string
    foodType: string;       // String or empty
    expiryDate: string;     // String or empty
}
  