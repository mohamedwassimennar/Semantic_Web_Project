import { Inventory } from "./inventory";

export class InventoryClass implements Inventory {
    inventory: string;
    currentQuantity: number;
    foodId: string;
    foodType: string;
    expiryDate: string;

    constructor(
        inventory: string = '',
        currentQuantity: number = 0,
        foodId: string = '',
        foodType: string = '',
        expiryDate: string = ''
    ) {
        this.inventory = inventory;
        this.currentQuantity = currentQuantity;
        this.foodId = foodId;
        this.foodType = foodType;
        this.expiryDate = expiryDate;
    }
}
  