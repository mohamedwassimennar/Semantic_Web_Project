import { Manager } from "./manager";

export class ManagerClass implements Manager {
    manager: string;
    name: string;
    contact: string;

    constructor(
        manager: string = '',
        name: string = '',
        contact: string = ''
    ) {
        this.manager = manager;
        this.name = name;
        this.contact = contact;
    }
}
  