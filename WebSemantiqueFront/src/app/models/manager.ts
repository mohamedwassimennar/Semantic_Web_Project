export interface ManagerResponse {
    results: Manager[];
}

export interface Manager {
    manager: string;    // URI like "http://rescuefood.org/ontology#Manager1"
    name: string;       // Manager's name
    contact: string;    // Contact number as string
}
  